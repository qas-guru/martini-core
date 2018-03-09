/*
Copyright 2018 Penny Rohr Curich

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import gherkin.ast.Location;
import gherkin.ast.Step;
import guru.qas.martini.MartiniException;
import guru.qas.martini.gherkin.FeatureWrapper;
import guru.qas.martini.gherkin.Recipe;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.ResourceBundle.getBundle;

@SuppressWarnings("WeakerAccess")
public class UnimplementedStepException extends MartiniException {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnimplementedStepException.class);
	private static final String RESOURCE_BUNDLE_KEY = "martini.unimplemented.step";

	protected UnimplementedStepException(String message) {
		super(message);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected static final String DEFAULT_TEMPLATE = "unimplemented step: %s line %s: @%s %s";
		protected static final String BASENAME = "guru.qas.martini.il8n.martini-il8n";

		private Recipe recipe;
		private Step step;

		protected Builder() {
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

			String message = getMessage();
			return new UnimplementedStepException(message);
		}

		private String getMessage() {
			String description = getDescription();
			int line = getLine();
			String keyword = getKeyword();
			String text = getText();

			String message;
			try {
				String template = getTemplate();
				message = String.format(template, description, line, keyword, text);
			}
			catch (Exception e) {
				message = String.format(DEFAULT_TEMPLATE, description, line, keyword, text);
			}
			return message;
		}

		protected String getTemplate() {
			String template = DEFAULT_TEMPLATE;

			try {
				String language = recipe.getPickle().getLanguage();
				Locale locale = new Locale(language);
				ResourceBundle bundle = getBundle(BASENAME, locale);
				template = bundle.getString(RESOURCE_BUNDLE_KEY);
			}
			catch (Exception e) {
				LOGGER.warn(
					"unable to retrieve value of key {} from ResourceBundle basename {} for locale {}",
					RESOURCE_BUNDLE_KEY, BASENAME, e);
			}
			return template;
		}

		private String getDescription() {
			FeatureWrapper featureWrapper = recipe.getFeatureWrapper();
			Resource resource = featureWrapper.getResource();
			return resource.getDescription();
		}

		private int getLine() {
			Location location = step.getLocation();
			return location.getLine();
		}

		private String getKeyword() {
			String keyword = step.getKeyword();
			return keyword.trim();
		}

		private String getText() {
			String text = step.getText();
			return text.trim();
		}
	}
}
