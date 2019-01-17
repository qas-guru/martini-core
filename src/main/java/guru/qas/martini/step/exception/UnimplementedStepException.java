/*
Copyright 2017-2019 Penny Rohr Curich

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

package guru.qas.martini.step.exception;

import javax.annotation.Nullable;

import org.springframework.core.io.Resource;

import exception.SkippedException;
import gherkin.ast.Location;
import gherkin.ast.Step;
import guru.qas.martini.gherkin.FeatureWrapper;
import guru.qas.martini.gherkin.Recipe;

import static com.google.common.base.Preconditions.checkNotNull;
import static guru.qas.martini.step.exception.UnimplementedStepExceptionMessages.*;

@SuppressWarnings("WeakerAccess")
public class UnimplementedStepException extends SkippedException {

	protected UnimplementedStepException(Enum<?> messageKey, @Nullable Object... messageArgs) {
		super(checkNotNull(messageKey, "null Enum"), messageArgs);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected Recipe recipe;
		protected Step step;

		protected Builder() {
			super();
		}

		public Builder setRecipe(Recipe r) {
			this.recipe = r;
			return this;
		}

		public Builder setStep(Step s) {
			this.step = s;
			return this;
		}

		public UnimplementedStepException build() {
			checkNotNull(recipe, "null Recipe");
			checkNotNull(step, "null Step");

			String description = getDescription();
			int line = getLine();
			String keyword = getKeyword();
			String text = getText();
			return new UnimplementedStepException(UNIMPLEMENTED_STEP, description, line, keyword, text);
		}

		protected Object[] getArguments() {
			String description = getDescription();
			int line = getLine();
			String keyword = getKeyword();
			String text = getText();
			return new Object[]{description, line, keyword, text};
		}

		protected String getDescription() {
			FeatureWrapper featureWrapper = recipe.getFeatureWrapper();
			Resource resource = featureWrapper.getResource();
			return resource.getDescription();
		}

		protected int getLine() {
			Location location = step.getLocation();
			return location.getLine();
		}

		protected String getKeyword() {
			String keyword = step.getKeyword();
			return keyword.trim();
		}

		protected String getText() {
			String text = step.getText();
			return text.trim();
		}
	}
}
