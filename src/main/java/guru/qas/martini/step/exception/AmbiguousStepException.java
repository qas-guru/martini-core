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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import gherkin.ast.Step;
import exception.MartiniException;
import guru.qas.martini.Messages;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;

import static com.google.common.base.Preconditions.*;
import static guru.qas.martini.step.exception.AmbiguousStepExceptionMessages.*;

@SuppressWarnings("WeakerAccess")
public class AmbiguousStepException extends MartiniException implements Serializable {

	private static final long serialVersionUID = -2801224092338477492L;

	public AmbiguousStepException(Enum<?> messageKey, @Nullable Object... messageArgs) {
		super(checkNotNull(messageKey, "null Enum"), messageArgs);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected Recipe recipe;
		protected Step step;
		protected List<StepImplementation> matches;

		protected Builder() {
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

		public AmbiguousStepException build() {
			checkState(null != step, "Step not set");
			checkState(!matches.isEmpty(), "Iterable<StepImplementation> not set");

			String id = recipe.getId();
			String stepDescription = getStepDescription(step);
			List<String> methodDescriptions = getMethodDescriptions();
			String joined = '\n' + Joiner.on('\n').join(methodDescriptions);
			return new AmbiguousStepException(AMBIGUOUS_STEP, id, stepDescription, joined);
		}

		protected List<String> getMethodDescriptions() {
			return matches.stream().map(Builder::getMethodDescription).collect(Collectors.toList());
		}

		protected static String getMethodDescription(StepImplementation match) {
			Method method = getMethod(match);
			Annotation[] annotations = method.getAnnotations();
			String joinedAnnotations = Joiner.on("\n\t").join(annotations);
			return Messages.getMessage(METHOD, method, joinedAnnotations);
		}

		private static Method getMethod(StepImplementation match) {
			return match.getMethod().orElseThrow(() -> {
				Class<? extends StepImplementation> implementation = match.getClass();
				String message = String.format("no method; is StepImplementation %s returning false positives on isMatch()?", implementation);
				throw new NullPointerException(message);
			});
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
	}
}
