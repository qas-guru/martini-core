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

package guru.qas.martini.event;

import java.io.Serializable;

import guru.qas.martini.gherkin.Recipe;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class ScenarioIdentifier implements Serializable {

	private static final long serialVersionUID = 6618021233346600615L;

	protected final SuiteIdentifier suiteIdentifier;
	protected final String threadName;
	protected final String recipeId;

	public SuiteIdentifier getSuiteIdentifier() {
		return suiteIdentifier;
	}

	public String getThreadName() {
		return threadName;
	}

	public String getRecipeId() {
		return recipeId;
	}

	protected ScenarioIdentifier(
		SuiteIdentifier suiteIdentifier,
		String threadName,
		String recipeId
	) {
		this.suiteIdentifier = suiteIdentifier;
		this.threadName = threadName;
		this.recipeId = recipeId;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected SuiteIdentifier identifier;
		protected String threadName;
		protected Recipe recipe;

		protected Builder() {
		}

		public Builder setSuiteIdentifier(SuiteIdentifier identifier) {
			this.identifier = identifier;
			return this;
		}

		public Builder setThreadName(String s) {
			this.threadName = s;
			return this;
		}

		public Builder setRecipe(Recipe r) {
			this.recipe = r;
			return this;
		}

		public ScenarioIdentifier build() {
			checkNotNull(identifier, "SuiteIdentifier not set");
			checkNotNull(recipe, "Recipe not set");
			String trimmedThread = checkNotNull(threadName, "thread name not set").trim();
			String recipeId = recipe.getId();
			return new ScenarioIdentifier(identifier, trimmedThread, recipeId);
		}
	}
}
