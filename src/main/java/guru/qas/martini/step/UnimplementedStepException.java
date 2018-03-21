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

package guru.qas.martini.step;

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.core.io.Resource;

import gherkin.ast.Location;
import gherkin.ast.Step;
import gherkin.pickles.Pickle;
import guru.qas.martini.MartiniException;
import guru.qas.martini.gherkin.FeatureWrapper;
import guru.qas.martini.gherkin.Recipe;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class UnimplementedStepException extends MartiniException {

	/**
	 * {@inheritDoc}
	 * <p>
	 * deprecated, use {@link guru.qas.martini.step.UnimplementedStepException.Builder new guru.qas.martini.step.UnimplementedStepException.Builder()}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	protected UnimplementedStepException(String message) {
		super(message);
	}

	public static class Builder extends MartiniException.Builder {

		protected static final String KEY = "martini.unimplemented.step";

		private Recipe recipe;
		private Step step;

		public Builder() {
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

		@SuppressWarnings("deprecation")
		@Override
		public UnimplementedStepException build() {
			checkNotNull(recipe, "null Recipe");
			checkNotNull(step, "null Step");

			ResourceBundle messageBundle = getMessageBundle();
			super.setResourceBundle(messageBundle);

			super.setKey(KEY);

			Object[] arguments = getArguments();
			super.setArguments(arguments);

			String message = getMessage();
			return new UnimplementedStepException(message);
		}

		protected ResourceBundle getMessageBundle() {
			Pickle pickle = recipe.getPickle();
			String language = pickle.getLanguage();
			Locale locale = new Locale(language);

			String baseName = UnimplementedStepException.class.getName();
			ClassLoader loader = UnimplementedStepException.class.getClassLoader();
			return ResourceBundle.getBundle(baseName, locale, loader);
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
