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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;

import gherkin.ast.Background;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import guru.qas.martini.gate.MartiniGate;
import guru.qas.martini.gate.MartiniGateFactory;
import guru.qas.martini.gherkin.GherkinResourceLoader;
import guru.qas.martini.gherkin.Mixology;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.i18n.MessageSources;
import guru.qas.martini.step.StepImplementation;
import guru.qas.martini.tag.Categories;

import static com.google.common.base.Preconditions.*;

/**
 * Default implementation of a Mixologist.
 */
@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultMartiniFactory implements MartiniFactory, ApplicationContextAware {

	protected final GherkinResourceLoader loader;
	protected final Mixology mixology;
	protected final Categories categories;
	protected final MartiniGateFactory gateFactory;

	protected ApplicationContext context;

	@Autowired
	protected DefaultMartiniFactory(
		GherkinResourceLoader loader,
		Mixology mixology,
		Categories categories,
		MartiniGateFactory gateFactory
	) {
		this.loader = loader;
		this.mixology = mixology;
		this.categories = categories;
		this.gateFactory = gateFactory;
	}

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext context) {
		this.context = checkNotNull(context, "null ApplicationContext");
	}

	@Override
	public Collection<Martini> getMartinis() {
		List<Resource> resources = getFeatureResources();
		List<Recipe> recipes = resources.stream()
			.flatMap(resource -> mixology.get(resource).stream())
			.collect(Collectors.toList());
		return getMartinis(recipes);
	}

	protected List<Resource> getFeatureResources() {
		try {
			Resource[] resources = loader.getFeatureResources();
			return Arrays.stream(resources).collect(Collectors.toList());
		}
		catch (IOException e) {
			MessageSource messageSource = MessageSources.getMessageSource(getClass());
			throw new MartiniException.Builder()
				.setCause(e)
				.setMessageSource(messageSource)
				.setKey("exception.loading.feature.resources")
				.build();
		}
	}

	protected Collection<Martini> getMartinis(Collection<Recipe> recipes) {
		return recipes.stream()
			.map(this::getMartini)
			.collect(Collectors.toList());
	}

	protected Martini getMartini(Recipe recipe) {

		AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
		DefaultStepImplementationResolver resolver = new DefaultStepImplementationResolver(recipe);
		beanFactory.autowireBean(resolver);

		Collection<Step> steps = getSteps(recipe);

		DefaultMartini.Builder builder = DefaultMartini.builder().setRecipe(recipe);
		steps.forEach(step -> {
			StepImplementation implementation = resolver.getImplementation(step);
			builder.add(step, implementation);
			Collection<MartiniGate> gates = gateFactory.getGates(implementation);
			builder.addAll(gates);
		});
		return builder.build();
	}

	protected Collection<Step> getSteps(Recipe recipe) {
		Background background = recipe.getBackground();
		ScenarioDefinition scenarioDefinition = recipe.getScenarioDefinition();
		List<Step> steps = new ArrayList<>();
		steps.addAll(null == background ? Collections.emptyList() : background.getSteps());
		steps.addAll(scenarioDefinition.getSteps());
		return steps;
	}
}
