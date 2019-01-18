/*
Copyright 2018 Penny Rohr Curich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package guru.qas.martini.runtime.harness;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.ConversionService;

import com.google.common.base.Throwables;

import ch.qos.cal10n.IMessageConveyor;
import exception.SkippedException;
import gherkin.ast.Examples;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import guru.qas.martini.Martini;
import exception.MartiniException;
import guru.qas.martini.Messages;
import guru.qas.martini.event.Status;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.result.DefaultMartiniResult;
import guru.qas.martini.result.DefaultStepResult;
import guru.qas.martini.result.MartiniResult;
import guru.qas.martini.result.StepResult;
import guru.qas.martini.runtime.event.EventManager;
import guru.qas.martini.step.StepImplementation;
import guru.qas.martini.step.exception.UnimplementedStepException;
import guru.qas.martini.tag.Categories;

import static com.google.common.base.Preconditions.*;
import static guru.qas.martini.runtime.harness.MartiniCallableMessages.*;

@SuppressWarnings({"WeakerAccess", "unused"})
@Configurable
public class MartiniCallable implements Callable<MartiniResult>, InitializingBean {

	private static final Pattern OUTLINE_PATTERN = Pattern.compile("^<(.*)>$");

	protected final Martini martini;

	protected BeanFactory beanFactory;
	protected SuiteIdentifier suiteIdentifier;
	protected EventManager eventManager;
	protected ConversionService conversionService;
	protected Categories categories;

	protected LocLogger logger;

	@Autowired
	protected void set(SuiteIdentifier i) {
		this.suiteIdentifier = i;
	}

	@Autowired
	protected void set(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Autowired
	protected void set(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	@Autowired
	protected void set(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Autowired
	protected void set(Categories categories) {
		this.categories = categories;
	}

	public MartiniCallable(Martini martini) {
		this.martini = checkNotNull(martini, "null Martini");
	}

	@SuppressWarnings("RedundantThrows")
	@Override
	public void afterPropertiesSet() throws Exception {
		setUpLogger();
	}

	protected void setUpLogger() {
		IMessageConveyor messageConveyor = Messages.getMessageConveyor();
		LocLoggerFactory loggerFactory = new LocLoggerFactory(messageConveyor);
		logger = loggerFactory.getLocLogger(this.getClass());
	}

	@Override
	public MartiniResult call() {
		logScenario();

		DefaultMartiniResult result = null;
		try {
			Set<String> categorizations = categories.getCategorizations(martini);
			result = DefaultMartiniResult.builder()
				.setMartiniSuiteIdentifier(suiteIdentifier)
				.setMartini(martini)
				.build(categories);

			eventManager.publishBeforeScenario(this, result);

			Map<Step, StepImplementation> stepIndex = martini.getStepIndex();

			DefaultStepResult stepResult = null;
			for (Map.Entry<Step, StepImplementation> mapEntry : stepIndex.entrySet()) {
				assertNotInterrupted();
				Step step = mapEntry.getKey();
				eventManager.publishBeforeStep(this, result);

				StepImplementation implementation = mapEntry.getValue();
				if (null == stepResult || Status.PASSED.equals(stepResult.getStatus().orElse(null))) {
					stepResult = execute(step, implementation);
				}
				else {
					stepResult = new DefaultStepResult(step, implementation);
					stepResult.setStatus(Status.SKIPPED);
				}

				assertNotInterrupted();
				result.add(stepResult);
				logStepResult(stepResult);
				eventManager.publishAfterStep(this, result);
			}
		}
		catch (RuntimeException e) {
			String stacktrace = Throwables.getStackTraceAsString(e);
			logger.warn(UNEXPECTED_EXCEPTION, martini, stacktrace);
			e.fillInStackTrace();
			throw e;
		}
		finally {
			if (null != result) {
				eventManager.publishAfterScenario(this, result);
			}
		}

		return result;
	}

	private void logScenario() {
		if (logger.isInfoEnabled()) {
			String id = martini.getId();
			logger.info(STARTING, id);
		}
	}

	protected DefaultStepResult execute(Step step, StepImplementation implementation) {
		logStep(step);

		DefaultStepResult result = new DefaultStepResult(step, implementation);
		result.setStartTimestamp(System.currentTimeMillis());
		try {
			Method method = implementation.getMethod().orElseThrow(() -> {
				Recipe recipe = martini.getRecipe();
				UnimplementedStepException exception =
					UnimplementedStepException.builder().setRecipe(recipe).setStep(step).build();
				String message = exception.getLocalizedMessage();
				logger.warn(message);
				return exception;
			});

			Object bean = getBean(method);
			Object[] arguments = getArguments(step, method, implementation);
			Object o = execute(method, bean, arguments);

			if (o instanceof HttpEntity) {
				result.add((HttpEntity) o);
			}
			result.setStatus(Status.PASSED);
		}
		catch (SkippedException e) {
			result.setException(e);
			result.setStatus(Status.SKIPPED);
		}
		catch (Exception e) {
			result.setException(e);
			result.setStatus(Status.FAILED);
		}
		finally {
			result.setEndTimestamp(System.currentTimeMillis());
		}
		return result;
	}

	private void logStep(Step step) {
		if (logger.isInfoEnabled()) {
			String scenarioId = getScenarioId();
			String keyword = step.getKeyword().trim();
			String text = step.getText().trim();
			logger.info(EXECUTING_STEP, scenarioId, keyword, text);
		}
	}

	private String getScenarioId() {
		Recipe recipe = martini.getRecipe();
		Pickle pickle = recipe.getPickle();
		String scenarioName = pickle.getName();
		PickleLocation location = recipe.getLocation();
		int line = location.getLine();
		return String.format("%s:%s", scenarioName, line);
	}

	protected void logStepResult(StepResult result) {
		result.getStatus().ifPresent(status -> {
			if (logger.isInfoEnabled()) {
				String scenarioId = getScenarioId();
				Step step = result.getStep();
				String keyword = step.getKeyword().trim();
				String stepText = step.getText().trim();

				switch (status) {
					case PASSED:
						logger.info(MartiniCallableMessages.PASSED, status, scenarioId, keyword, stepText);
						break;
					case FAILED:
						Exception exception = result.getException().orElse(null);
						if (null == exception) {
							logger.info(FAILED, status, scenarioId, keyword, stepText);
						}
						else {
							String stacktrace = '\n' + Throwables.getStackTraceAsString(exception);
							logger.info(FAILED_WITH_EXCEPTION, status, scenarioId, keyword, stepText, stacktrace);
						}
						break;
					default:
						logger.warn(SKIPPED, status, scenarioId, keyword, stepText);
						break;
				}
			}
		});
	}

	protected Object[] getArguments(Step step, Method method, StepImplementation implementation) {
		Parameter[] parameters = method.getParameters();
		Object[] arguments = new Object[parameters.length];

		Map<String, String> exampleValues = null;

		Recipe recipe = martini.getRecipe();
		ScenarioDefinition definition = recipe.getScenarioDefinition();
		if (definition instanceof ScenarioOutline) {
			exampleValues = new HashMap<>();
			ScenarioOutline outline = (ScenarioOutline) definition;

			int exampleLine = recipe.getLocation().getLine();

			List<Examples> examples = outline.getExamples();
			TableRow header = null;
			TableRow match = null;
			for (Iterator<Examples> i = examples.iterator(); null == match && i.hasNext(); ) {
				Examples nextExamples = i.next();
				List<TableRow> rows = nextExamples.getTableBody();
				for (Iterator<TableRow> j = rows.iterator(); null == match && j.hasNext(); ) {
					TableRow row = j.next();
					if (row.getLocation().getLine() == exampleLine) {
						match = row;
						header = nextExamples.getTableHeader();
					}
				}
			}

			checkState(null != header, Messages.getMessage(MISSING_EXAMPLES));
			List<TableCell> headerCells = header.getCells();
			List<TableCell> rowCells = match.getCells();
			checkState(headerCells.size() == rowCells.size(), Messages.getMessage(INVALID_EXAMPLES_HEADER));
			for (int i = 0; i < headerCells.size(); i++) {
				String headerValue = headerCells.get(i).getValue();
				String rowValue = rowCells.get(i).getValue();
				exampleValues.put(headerValue, rowValue);
			}
		}

		if (parameters.length > 0) {
			Matcher matcher = getMatcher(step, implementation);

			int groupCount = matcher.groupCount();
			for (int i = 0; i < groupCount; i++) {
				String parameterAsString = matcher.group(i + 1);
				Parameter parameter = parameters[i];
				Class<?> parameterType = parameter.getType();

				Object converted;
				if (null == exampleValues) {
					converted = conversionService.convert(parameterAsString, parameterType);
				}
				else {
					Matcher tableMatcher = OUTLINE_PATTERN.matcher(parameterAsString);
					checkState(tableMatcher.find(), Messages.getMessage(INVALID_EXAMPLES_FORMAT));
					String key = tableMatcher.group(1);
					String tableValue = exampleValues.get(key);
					converted = conversionService.convert(tableValue, parameterType);
				}

				arguments[i] = converted;
			}
		}
		return arguments;
	}

	protected Matcher getMatcher(Step step, StepImplementation implementation) {
		Pattern pattern = implementation.getPattern().orElse(null);

		Matcher matcher = null;
		if (null != pattern) {
			String text = step.getText();
			matcher = pattern.matcher(text);
			checkState(matcher.find(), Messages.getMessage(INVALID_SUBSTITUTION, pattern.pattern(), text));
		}

		return checkNotNull(matcher, Messages.getMessage(NO_MATCHER));
	}

	protected Object getBean(Method method) {
		Class<?> declaringClass = method.getDeclaringClass();
		return beanFactory.getBean(declaringClass);
	}

	protected Object execute(Method method, Object bean, Object[] arguments) throws MartiniException {
		assertNotInterrupted();

		try {
			return method.invoke(bean, arguments);
		}
		catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (MartiniException.class.isInstance(cause)) {
				throw MartiniException.class.cast(cause);
			}
			else {
				throw new MartiniException(cause, EXECUTION_EXCEPTION);
			}
		}
		catch (Exception e) {
			throw new MartiniException(e, EXECUTION_EXCEPTION);
		}
	}

	protected void assertNotInterrupted() {
		Thread thread = Thread.currentThread();
		checkState(!thread.isInterrupted(), Messages.getMessage(INTERRUPTED));
	}
}
