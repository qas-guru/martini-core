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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import gherkin.ast.Step;
import guru.qas.martini.DefaultMartini;
import guru.qas.martini.MartiniException;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.i18n.MessageSources;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("WeakerAccess")
public class AmbiguousStepException extends MartiniException {

	/**
	 * {@inheritDoc}
	 * <p>
	 * deprecated, use {@link guru.qas.martini.step.AmbiguousStepException.Builder new guru.qas.martini.step.AmbiguousStepException.Builder()}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	protected AmbiguousStepException(String message) {
		super(message);
	}

	public static class Builder extends MartiniException.Builder {

		protected static final String KEY = "martini.ambiguous.step";
		protected static final String KEY_DETAIL = "martini.ambigious.step.detail";

		protected Recipe recipe;
		protected Step step;
		protected List<StepImplementation> matches;

		public Builder() {
			super();
			this.matches = new ArrayList<>();
		}

		public Builder setRecipe(Recipe r) {
			this.recipe = r;
			return this;
		}

		public Builder setStep(Step s) {
			this.step = s;
			return this;
		}

		public Builder setMatches(Iterable<StepImplementation> matches) {
			this.matches.clear();
			if (null != matches) {
				Lists.newArrayList(matches).stream()
					.filter(Objects::nonNull)
					.forEach(m -> this.matches.add(m));
			}
			return this;
		}

		@SuppressWarnings("deprecation")
		public AmbiguousStepException build() {
			checkState(null != step, "Step not set");
			checkState(!matches.isEmpty(), "Iterable<StepImplementation> not set");

			MessageSource messageSource = MessageSources.getMessageSource(DefaultMartini.class);
			super.setMessageSource(messageSource);

			Object[] arguments = getArguments();

			super.setKey(KEY);
			super.setArguments(arguments);
			String message = super.getMessage();
			return new AmbiguousStepException(message);
		}

		protected Object[] getArguments() {
			List<String> details = getDetails();
			String summary = Joiner.on('\n').join(details);
			String description = getStepDescription(step);
			return new Object[]{description, summary};
		}

		protected String getStepDescription(Step step) {
			String keyword = getKeyword(step);
			String text = step.getText();
			return keyword.isEmpty() ? text : String.format("%s %s", keyword, text);
		}

		protected String getKeyword(Step step) {
			String keyword = step.getKeyword();
			return null == keyword ? "" : keyword.trim();
		}

		protected List<String> getDetails() {
			return matches.stream().map(this::getDetail).collect(Collectors.toList());
		}

		protected String getDetail(StepImplementation match) {
			Method method = match.getMethod();
			Annotation[] annotations = method.getAnnotations();
			String joinedAnnotations = Joiner.on("\n\t").join(annotations);

			super.setKey(KEY_DETAIL);
			super.setArguments(method, joinedAnnotations);
			return super.getMessage();
		}
	}
}
