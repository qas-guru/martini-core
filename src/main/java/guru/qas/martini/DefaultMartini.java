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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.context.MessageSource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import guru.qas.martini.gate.MartiniGate;
import guru.qas.martini.gherkin.FeatureWrapper;
import guru.qas.martini.i18n.MessageSources;
import guru.qas.martini.tag.DefaultMartiniTag;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;
import guru.qas.martini.tag.MartiniTag;

import static com.google.common.base.Preconditions.*;

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
	public ImmutableSet<MartiniGate> getGates() {
		return gates;
	}

	@Override
	public ImmutableSet<MartiniTag> getTags() {
		return tags;
	}

	protected DefaultMartini(
		@Nonnull Recipe recipe,
		@Nonnull ImmutableMap<Step, StepImplementation> stepIndex,
		@Nonnull ImmutableSet<MartiniGate> gates,
		@Nonnull ImmutableSet<MartiniTag> tags
	) {
		this.recipe = checkNotNull(recipe, "null Recipe");
		this.stepIndex = checkNotNull(stepIndex, "null ImmutableMap");
		this.gates = checkNotNull(gates, "null ImmutableSet<MartiniGate>");
		this.tags = checkNotNull(tags, "null ImmutableSet<MartiniTag>");
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
			Method method = implementation.getMethod().orElse(null);
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

		protected Recipe recipe;
		protected final LinkedHashMap<Step, StepImplementation> index;
		protected final LinkedHashSet<MartiniGate> gates;

		protected Builder() {
			index = new LinkedHashMap<>();
			gates = new LinkedHashSet<>();
		}

		protected Builder setRecipe(@Nullable Recipe recipe) {
			this.recipe = recipe;
			return this;
		}

		protected Builder add(@Nonnull Step step, @Nullable StepImplementation implementation) {
			checkNotNull(step, "null Step");
			index.put(step, implementation);
			return this;
		}

		@SuppressWarnings("UnusedReturnValue")
		protected Builder addAll(@Nullable Collection<MartiniGate> gates) {
			if (null != gates) {
				gates.stream().filter(Objects::nonNull).forEach(this.gates::add);
			}
			return this;
		}

		protected DefaultMartini build() {
			ImmutableSet<MartiniTag> tags = getTags();
			ImmutableMap<Step, StepImplementation> immutableIndex = ImmutableMap.copyOf(index);
			ImmutableSet<MartiniGate> immutableGates = ImmutableSet.copyOf(gates);
			return new DefaultMartini(recipe, immutableIndex, immutableGates, tags);
		}

		protected ImmutableSet<MartiniTag> getTags() {
			Pickle pickle = null == recipe ? null : recipe.getPickle();
			return null == pickle ? ImmutableSet.of() : getTags(pickle);
		}

		protected ImmutableSet<MartiniTag> getTags(Pickle pickle) {
			List<PickleTag> pickleTags = pickle.getTags();
			List<MartiniTag> martiniTags = null == pickleTags ?
				ImmutableList.of() : pickleTags.stream().map(this::getTag).collect(Collectors.toList());
			return ImmutableSet.copyOf(martiniTags);
		}

		protected MartiniTag getTag(PickleTag tag) {
			try {
				return DefaultMartiniTag.builder().setPickleTag(tag).build();
			}
			catch (Exception e) {
				MessageSource messageSource = MessageSources.getMessageSource(DefaultMartini.class);
				throw new MartiniException.Builder().setCause(e)
					.setMessageSource(messageSource).setKey(MESSAGE_KEY).setArguments(recipe.getId()).build();
			}
		}
	}
}
