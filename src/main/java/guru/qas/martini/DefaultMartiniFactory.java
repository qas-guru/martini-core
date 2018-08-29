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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.google.common.collect.ImmutableList;

import gherkin.ast.Background;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import guru.qas.martini.gate.MartiniGate;
import guru.qas.martini.gate.MartiniGateFactory;
import guru.qas.martini.gherkin.GherkinResourceLoader;
import guru.qas.martini.gherkin.Mixology;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.AmbiguousStepException;
import guru.qas.martini.step.StepImplementation;
import guru.qas.martini.step.UnimplementedStep;
import guru.qas.martini.step.UnimplementedStepException;
import guru.qas.martini.tag.Categories;

import static com.google.common.base.Preconditions.*;

/**
 * Default implementation of a Mixologist.
 */
@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultMartiniFactory implements MartiniFactory, InitializingBean, ApplicationContextAware {

	protected final GherkinResourceLoader loader;
	protected final Mixology mixology;
	protected final Categories categories;
	protected final MartiniGateFactory gateFactory;
	protected final boolean unimplementedStepsFatal;

	protected ApplicationContext context;
	protected ImmutableList<StepImplementation> stepImplementations;
	protected ImmutableList<Martini> martinis;

	@Override
	public Collection<Martini> getMartinis() {
		return martinis;
	}

	@Autowired
	protected DefaultMartiniFactory(
		GherkinResourceLoader loader,
		Mixology mixology,
		Categories categories,
		MartiniGateFactory gateFactory,
		@Value("${unimplemented.steps.fatal:#{false}}") boolean missingStepFatal
	) {
		this.loader = loader;
		this.mixology = mixology;
		this.categories = categories;
		this.gateFactory = gateFactory;
		this.unimplementedStepsFatal = missingStepFatal;
	}

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext context) {
		this.context = checkNotNull(context, "null ApplicationContext");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initializeStepImplementations();
		initializeMartinis();
	}

	protected void initializeStepImplementations() {
		ImmutableList.Builder<StepImplementation> builder = ImmutableList.builder();
		Map<String, StepImplementation> beans = context.getBeansOfType(StepImplementation.class);
		beans.values().forEach(builder::add);
		stepImplementations = builder.build();
	}

	protected void initializeMartinis() throws IOException {
		Resource[] resources = loader.getFeatureResources();
		List<Recipe> recipes = Arrays.stream(resources)
			.flatMap(resource -> mixology.get(resource).stream())
			.collect(Collectors.toList());
		initializeMartinis(recipes);
	}

	protected void initializeMartinis(Collection<Recipe> recipes) {
		ImmutableList.Builder<Martini> builder = ImmutableList.builder();
		recipes.stream().map(this::getMartini).forEach(builder::add);
		martinis = builder.build();
	}

	protected Martini getMartini(Recipe recipe) {
		List<Step> steps = getSteps(recipe);

		DefaultMartini.Builder builder = DefaultMartini.builder().setRecipe(recipe);
		steps.forEach(step -> {
			StepImplementation implementation = getImplementation(recipe, step);
			builder.add(step, implementation);
			Collection<MartiniGate> gates = gateFactory.getGates(implementation);
			builder.addAll(gates);
		});
		return builder.build();
	}

	protected List<Step> getSteps(Recipe recipe) {
		Background background = recipe.getBackground();
		ScenarioDefinition scenarioDefinition = recipe.getScenarioDefinition();
		List<Step> steps = new ArrayList<>();
		steps.addAll(null == background ? Collections.emptyList() : background.getSteps());
		steps.addAll(scenarioDefinition.getSteps());
		return steps;
	}

	protected StepImplementation getImplementation(Recipe recipe, Step step) {
		List<StepImplementation> matches = stepImplementations.stream()
			.filter(i -> i.isMatch(step))
			.collect(Collectors.toList());

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

	protected UnimplementedStep getUnimplemented(Step step) {
		String keyword = step.getKeyword();
		return new UnimplementedStep(null == keyword ? "" : keyword.trim());
	}

	//		LinkedHashMap<Step, StepImplementation> index = new LinkedHashMap<>();
	//		getPickleSteps(recipe).stream()
	//			.map(pickleStep -> getGherkinStep(recipe, pickleStep))
	//			.forEach(gherkinStep -> {
	//				StepImplementation implementation = getImplementation(recipe, gherkinStep, stepImplementations);
	//				index.put(gherkinStep, implementation);
	//			});
	//
	//		return getMartini(recipe, index);

//	protected Martini getMartini(Recipe recipe, LinkedHashMap<Step, StepImplementation> index) {
//		LinkedHashSet<String> gateNames = new LinkedHashSet<>();
//		HashMultimap<String, MartiniGate> gateIndex = HashMultimap.create();
//
//		index.values().stream()
//			.filter(Objects::nonNull)
//			.map(gateFactory::getGates)
//			.forEach(c -> c.forEach(gate -> {
//				String name = gate.getName();
//				gateNames.add(name);
//				gateIndex.put(name, gate);
//			}));
//
//		Comparator<String> keyComparator = Ordering.explicit(new ArrayList<>(gateNames));
//		Comparator<MartiniGate> valueComparator = Ordering.natural().onResultOf(gate -> null == gate ? Integer.MAX_VALUE : gate.getPriority());
//		TreeMultimap<String, MartiniGate> orderedGateIndex = TreeMultimap.create(keyComparator, valueComparator);
//
//		return DefaultMartini.builder()
//			.setRecipe(recipe)
//			.add(Maps.immutableEntry(step, stepImplementation))
//			.add(gates)
//			.build();
//	}
}
