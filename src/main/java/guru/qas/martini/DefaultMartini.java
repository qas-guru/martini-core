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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.springframework.context.MessageSource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import guru.qas.martini.annotation.Gated;
import guru.qas.martini.gate.MartiniGate;
import guru.qas.martini.gate.MartiniGateFactory;
import guru.qas.martini.gherkin.FeatureWrapper;
import guru.qas.martini.i18n.MessageSources;
import guru.qas.martini.tag.DefaultMartiniTag;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;
import guru.qas.martini.tag.MartiniTag;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of a Martini.
 */
@SuppressWarnings("WeakerAccess")
public class DefaultMartini implements Martini {

	protected final Recipe recipe;
	protected final ImmutableMap<Step, StepImplementation> stepIndex;
	protected final ImmutableSet<MartiniGate> gates;
	protected final ImmutableSet<MartiniTag> tags;

	@Override
	public Recipe getRecipe() {
		return recipe;
	}

	@Override
	public ImmutableMap<Step, StepImplementation> getStepIndex() {
		return stepIndex;
	}

	@Override
	public Collection<MartiniGate> getGates() {
		return gates;
	}

	@Override
	public Collection<MartiniTag> getTags() {
		return tags;
	}

	protected DefaultMartini(
		@Nonnull Recipe recipe,
		@Nonnull Map<Step, StepImplementation> stepIndex,
		MartiniGate[] gates,
		MartiniTag[] tags
	) {
		this.recipe = checkNotNull(recipe, "null Recipe");
		this.stepIndex = ImmutableMap.copyOf(checkNotNull(stepIndex, "null Map"));
		this.gates = null == gates ? ImmutableSet.of() : ImmutableSet.<MartiniGate>builder().add(gates).build();
		this.tags = null == tags ? ImmutableSet.of() : ImmutableSet.<MartiniTag>builder().add(tags).build();
	}

	@Override
	public String getId() {
		return recipe.getId();
	}

	@Override
	public String getFeatureName() {
		FeatureWrapper feature = getRecipe().getFeatureWrapper();
		return feature.getName();
	}

	@Override
	public String getScenarioName() {
		ScenarioDefinition definition = getRecipe().getScenarioDefinition();
		return definition.getName();
	}

	@Override
	public int getScenarioLine() {
		PickleLocation location = getRecipe().getLocation();
		return location.getLine();
	}

	@Override
	public boolean isAnyStepAnnotated(Class<? extends Annotation> annotationClass) {
		checkNotNull(annotationClass, "null Class");
		Map<Step, StepImplementation> index = getStepIndex();

		boolean evaluation = false;
		Iterator<Map.Entry<Step, StepImplementation>> i = index.entrySet().iterator();
		while (!evaluation && i.hasNext()) {
			Map.Entry<Step, StepImplementation> mapEntry = i.next();
			StepImplementation implementation = mapEntry.getValue();
			Method method = implementation.getMethod();
			evaluation = null != method && method.isAnnotationPresent(annotationClass);
		}
		return evaluation;
	}

	@Override
	public String toString() {
		return getId();
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return Martini.class.isInstance(obj) && Martini.class.cast(obj).getId().equals(getId());
	}

	protected static Builder builder() {
		return new Builder();
	}

	protected static class Builder {

		protected static final String MESSAGE_KEY = "martini.creation.exception";

		private Recipe recipe;
		private LinkedHashMap<Step, StepImplementation> index;

		protected Builder() {
			index = Maps.newLinkedHashMap();
		}

		protected Builder setRecipe(Recipe recipe) {
			this.recipe = recipe;
			return this;
		}

		protected Builder setStepImplementationIndex(LinkedHashMap<Step, StepImplementation> index) {
			this.index.clear();
			if (null != index) {
				this.index.putAll(index);
			}
			return this;
		}

		protected DefaultMartini build(MartiniGateFactory gateFactory) {
			MartiniTag[] tags = getTags();
			MartiniGate[] gates = getGates(gateFactory);
			return new DefaultMartini(recipe, index, gates, tags);
		}

		protected MartiniTag[] getTags() {
			Pickle pickle = recipe.getPickle();
			List<PickleTag> pickleTags = pickle.getTags();

			return pickleTags.stream()
				.map(this::getDefaultMartiniTag)
				.toArray(MartiniTag[]::new);
		}

		protected DefaultMartiniTag getDefaultMartiniTag(PickleTag tag) {
			try {
				DefaultMartiniTag.Builder builder = DefaultMartiniTag.builder();
				return builder.setPickleTag(tag).build();
			}
			catch (Exception e) {
				MessageSource messageSource = MessageSources.getMessageSource(DefaultMartini.class);
				throw new MartiniException.Builder()
					.setCause(e)
					.setMessageSource(messageSource)
					.setKey(MESSAGE_KEY)
					.setArguments(recipe.getId())
					.build();
			}
		}

		protected MartiniGate[] getGates(MartiniGateFactory factory) {
			LinkedHashSet<String> names = new LinkedHashSet<>();
			index.values().stream()
				.map(StepImplementation::getMethod)
				.filter(Objects::nonNull)
				.forEach(m -> {
					Arrays.stream(m.getDeclaringClass().getDeclaredAnnotationsByType(Gated.class))
						.map(g -> g.name().trim())
						.forEach(names::add);

					Arrays.stream(m.getDeclaredAnnotationsByType(Gated.class))
						.map(g -> g.name().trim())
						.forEach(names::add);
				});
			return getGates(factory, names);
		}

		protected MartiniGate[] getGates(MartiniGateFactory factory, Collection<String> names) {
			return names.stream()
				.map(factory::getGate)
				.toArray(MartiniGate[]::new);
		}
	}
}
