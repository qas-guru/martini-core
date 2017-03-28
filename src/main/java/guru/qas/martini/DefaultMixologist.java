/*
Copyright 2017 Penny Rohr Curich

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

package guru.qas.martini;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.expression.Expression;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import guru.qas.martini.tag.Categories;
import gherkin.ast.Location;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import guru.qas.martini.gherkin.GherkinResourceLoader;
import guru.qas.martini.gherkin.Mixology;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;
import guru.qas.martini.tag.TagResolver;

import static com.google.common.base.Preconditions.*;

/**
 * Default implementation of a Mixologist.
 */
@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultMixologist implements Mixologist, InitializingBean, ApplicationContextAware {

	protected final GherkinResourceLoader loader;
	protected final Mixology mixology;
	protected final Categories categories;
	protected final boolean unimplementedStepsFatal;
	protected final AtomicReference<ImmutableList<Martini>> martinisReference;

	protected ApplicationContext context;
	protected ImmutableList<Recipe> recipes;

	@Autowired
	protected DefaultMixologist(
		GherkinResourceLoader loader,
		Mixology mixology,
		Categories categories,
		@Value("${unimplemented.steps.fatal:#{false}}") boolean missingStepFatal
	) {
		this.loader = loader;
		this.mixology = mixology;
		this.categories = categories;
		this.unimplementedStepsFatal = missingStepFatal;
		this.martinisReference = new AtomicReference<>();
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		recipes = null;
		martinisReference.set(null);
		Resource[] resources = loader.getFeatureResources();
		initialize(resources);
	}

	protected void initialize(Resource[] resources) throws IOException {
		ImmutableList.Builder<Recipe> builder = ImmutableList.builder();
		for (Resource resource : resources) {
			Iterable<Recipe> recipes = mixology.get(resource);
			builder.addAll(recipes);
		}
		this.recipes = builder.build();
	}

	@Override
	public ImmutableList<Martini> getMartinis() {
		synchronized (martinisReference) {
			ImmutableList<Martini> martinis = martinisReference.get();
			if (null == martinis) {
				Map<String, StepImplementation> index = context.getBeansOfType(StepImplementation.class);
				Collection<StepImplementation> implementations = index.values();

				ImmutableList.Builder<Martini> builder = ImmutableList.builder();
				for (Recipe recipe : recipes) {
					DefaultMartini.Builder martiniBuilder = DefaultMartini.builder().setRecipe(recipe);
					Pickle pickle = recipe.getPickle();
					ScenarioDefinition scenarioDefinition = recipe.getScenarioDefinition();
					List<PickleStep> steps = pickle.getSteps();
					for (PickleStep step : steps) {
						Step gherkinStep = getGherkinStep(scenarioDefinition, step);
						StepImplementation implementation = getImplementation(gherkinStep, implementations);
						martiniBuilder.add(gherkinStep, implementation);
					}
					Martini martini = martiniBuilder.build();
					builder.add(martini);
				}
				martinis = builder.build();
				martinisReference.set(martinis);
			}
			return martinis;
		}
	}

	private Step getGherkinStep(ScenarioDefinition definition, PickleStep step) {
		List<Step> steps = definition.getSteps();
		List<PickleLocation> locations = step.getLocations();
		Set<Integer> lines = Sets.newHashSetWithExpectedSize(locations.size());
		for (PickleLocation location : locations) {
			int line = location.getLine();
			lines.add(line);
		}

		Step gherkinStep = null;
		for (Iterator<Step> i = steps.iterator(); gherkinStep == null && i.hasNext(); ) {
			Step candidate = i.next();
			Location location = candidate.getLocation();
			int line = location.getLine();
			gherkinStep = lines.contains(line) ? candidate : null;
		}
		checkState(null != gherkinStep, "unable to locate Step %s in ScenarioDefinition %s", step, definition);
		return gherkinStep;
	}

	protected StepImplementation getImplementation(
		gherkin.ast.Step step, Collection<StepImplementation> implementations
	) {

		List<StepImplementation> matches = Lists.newArrayList();
		for (StepImplementation implementation : implementations) {
			if (implementation.isMatch(step)) {
				matches.add(implementation);
			}
		}

		int count = matches.size();
		if (count > 1) {
			String message = getAmbiguousStepMessage(step, matches);
			throw new IllegalStateException(message);
		}
		checkState(!unimplementedStepsFatal || 1 == count, "no implementation found matching step %s", step);
		return 0 == count ? StepImplementation.UNIMPLEMENTED : matches.get(0);
	}

	@SuppressWarnings("Guava")
	protected String getAmbiguousStepMessage(gherkin.ast.Step step, Collection<StepImplementation> implementations) {
		int count = implementations.size();

		List<String> details = Lists.newArrayListWithExpectedSize(count);
		for (StepImplementation implementation : implementations) {
			Method method = implementation.getMethod();
			Annotation[] annotations = method.getAnnotations();
			String joinedAnnotations = Joiner.on("\n\t").join(annotations);
			String detail = String.format("%s\nwith annotations\n\t%s", method, joinedAnnotations);
			details.add(detail);
		}
		String summary = Joiner.on('\n').join(details);
		String stepText = step.getText();
		return String.format("ambiguous step; %s matches found for step: %s\n%s", count, stepText, summary);
	}

	@Override
	public Collection<Martini> getMartinis(@Nullable String spelFilter) {
		String trimmed = null == spelFilter ? "" : spelFilter.trim();

		Collection<Martini> martinis = null;
		if (!trimmed.isEmpty()) {
			SpelExpressionParser parser = new SpelExpressionParser();
			Expression expression = parser.parseExpression(spelFilter);
			martinis = getMartinis(expression);
		}
		return null == martinis ? getMartinis() : martinis;
	}

	protected Collection<Martini> getMartinis(Expression expression) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		List<MethodResolver> methodResolvers = context.getMethodResolvers();
		ArrayList<MethodResolver> modifiedList = Lists.newArrayList(methodResolvers);
		modifiedList.add(new TagResolver(categories));
		context.setMethodResolvers(modifiedList);

		ImmutableList<Martini> martinis = getMartinis();
		List<Martini> matches = Lists.newArrayListWithCapacity(martinis.size());
		for (Martini martini : martinis) {
			Boolean match = expression.getValue(context, martini, Boolean.class);
			if (match) {
				matches.add(martini);
			}
		}
		return matches;
	}
}
