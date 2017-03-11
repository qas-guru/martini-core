package guru.qas.martini;

import java.util.LinkedHashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import gherkin.ast.Feature;
import gherkin.ast.Step;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;

@SuppressWarnings("WeakerAccess")
public class DefaultMartini implements Martini {

	protected final Recipe recipe;
	protected final ImmutableMap<Step, StepImplementation> stepIndex;

	@Override
	public Recipe getRecipe() {
		return recipe;
	}

	protected DefaultMartini(Recipe recipe, ImmutableMap<Step, StepImplementation> stepIndex) {
		this.recipe = recipe;
		this.stepIndex = stepIndex;
	}

	@Override
	public String toString() {
		Feature feature = recipe.getFeature();
		Pickle pickle = recipe.getPickle();
		PickleLocation location = pickle.getLocations().get(0);
		return String.format("Feature: %s\nResource: %s\nScenario: %s\nLine: %s",
			feature.getName(),
			location.getPath(),
			pickle.getName(),
			location.getLine());
	}

	protected static Builder builder() {
		return new Builder();
	}

	protected static class Builder {

		private Recipe recipe;
		private LinkedHashMap<Step, StepImplementation> index;

		protected Builder() {
			index = Maps.newLinkedHashMap();
		}

		protected Builder setRecipe(Recipe recipe) {
			this.recipe = recipe;
			return this;
		}

		protected Builder add(Step step, StepImplementation implementation) {
			index.put(step, implementation);
			return this;
		}

		protected DefaultMartini build() {
			ImmutableMap<Step, StepImplementation> immutableIndex = ImmutableMap.copyOf(index);
			return new DefaultMartini(recipe, immutableIndex);
		}
	}
}
