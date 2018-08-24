/*
Copyright 2017-2018 Penny Rohr Curich

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import gherkin.ast.Background;
import guru.qas.martini.step.AmbiguousStepException;
import guru.qas.martini.step.UnimplementedStep;
import guru.qas.martini.step.UnimplementedStepException;
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
	public void setApplicationContext(@Nonnull ApplicationContext context) throws BeansException {
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
			ImmutableList<Martini> list = martinisReference.get();
			if (null == list) {
				Map<String, StepImplementation> index = context.getBeansOfType(StepImplementation.class);
				Collection<StepImplementation> implementations = index.values();

				List<Martini> martinis = recipes.stream().map(r -> getMartini(implementations, r))
					.collect(Collectors.toList());
				list = ImmutableList.copyOf(martinis);
				martinisReference.set(list);
			}
			return list;
		}
	}

	protected Martini getMartini(Collection<StepImplementation> implementations, Recipe recipe) {
		DefaultMartini.Builder martiniBuilder = DefaultMartini.builder().setRecipe(recipe);

		getPickleSteps(recipe).stream()
			.map(pickleStep -> getGherkinStep(recipe, pickleStep))
			.forEach(gherkinStep -> {
				StepImplementation implementation = getImplementation(recipe, gherkinStep, implementations);
				martiniBuilder.add(gherkinStep, implementation);
			});

		return martiniBuilder.build();
	}

	protected List<PickleStep> getPickleSteps(Recipe recipe) {
		Pickle pickle = recipe.getPickle();
		return pickle.getSteps();
	}

	private Step getGherkinStep(Recipe recipe, PickleStep step) {
		Background background = recipe.getBackground();
		ScenarioDefinition scenarioDefinition = recipe.getScenarioDefinition();
		List<Step> steps = new ArrayList<>();
		steps.addAll(null == background ? Collections.emptyList() : background.getSteps());
		steps.addAll(scenarioDefinition.getSteps());

		List<PickleLocation> locations = step.getLocations();
		Set<Integer> lines = locations.stream().map(PickleLocation::getLine).collect(Collectors.toSet());

		return steps.stream()
			.filter(s -> lines.contains(getLine(s)))
			.findFirst()
			.orElseThrow(() -> {
				String message = String.format("unable to locate Step %s in ScenarioDefinition %s", step, scenarioDefinition);
				return new IllegalStateException(message);
			});
	}

	protected static int getLine(Step step) {
		Location location = step.getLocation();
		return location.getLine();
	}

	protected StepImplementation getImplementation(
		Recipe recipe,
		gherkin.ast.Step step,
		Collection<StepImplementation> implementations
	) {
		List<StepImplementation> matches = implementations.stream()
			.filter(i -> i.isMatch(step)).collect(Collectors.toList());

		StepImplementation match;

		int count = matches.size();
		if (1 == count) {
			match = matches.get(0);
		}
		else if (count > 1) {
			throw new AmbiguousStepException.Builder().setStep(step).setMatches(matches).build();
		}
		else if (unimplementedStepsFatal) {
			throw new UnimplementedStepException.Builder().setRecipe(recipe).setStep(step).build();
		}
		else {
			match = getUnimplemented(step);
		}
		return match;
	}

	@Override
	public Collection<Martini> getMartinis(@Nullable String spelFilter) {
		String trimmed = null == spelFilter ? "" : spelFilter.trim();

		Collection<Martini> martinis = null;
		if (!trimmed.isEmpty()) {
			SpelExpressionParser parser = new SpelExpressionParser();
			Expression expression = parser.parseExpression(trimmed);
			martinis = getMartinis(expression);
		}
		return null == martinis ? getMartinis() : martinis;
	}

	protected Collection<Martini> getMartinis(Expression expression) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		List<MethodResolver> methodResolvers = context.getMethodResolvers();
		ArrayList<MethodResolver> modifiedList = Lists.newArrayList(methodResolvers);
		modifiedList.add(new TagResolver(this.context, categories));
		context.setMethodResolvers(modifiedList);

		ImmutableList<Martini> martinis = getMartinis();
		List<Martini> matches = Lists.newArrayListWithCapacity(martinis.size());
		for (Martini martini : martinis) {
			Boolean match = expression.getValue(context, martini, Boolean.class);
			if (null != match && match) {
				matches.add(martini);
			}
		}
		return matches;
	}

	protected UnimplementedStep getUnimplemented(Step step) {
		String keyword = step.getKeyword();
		return new UnimplementedStep(null == keyword ? "" : keyword.trim());
	}
}
