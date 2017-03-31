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

package guru.qas.martini.step;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import gherkin.ast.Step;
import guru.qas.martini.MartiniException;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("WeakerAccess")
public class AmbiguousStepException extends MartiniException {

	protected AmbiguousStepException(String message) {
		super(message);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected Step step;
		protected ImmutableList<StepImplementation> matches;

		protected Builder() {
		}

		public Builder setStep(Step step) {
			this.step = step;
			return this;
		}

		public Builder setMatches(Iterable<StepImplementation> matches) {
			this.matches = null == matches ? null : ImmutableList.copyOf(matches);
			return this;
		}

		public AmbiguousStepException build() {
			checkState(null != step, "null Step");
			checkState(null != matches && !matches.isEmpty(), "null or empty Iterable<StepImplementation>");
			String message = getMessage();
			return new AmbiguousStepException(message);
		}

		protected String getMessage() {
			List<String> details = getDetails();
			String summary = Joiner.on('\n').join(details);
			String description = getStepDescription(step);
			return String.format("ambiguous step; multiple matches found for step: %s\n%s", description, summary);
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
			List<String> details = Lists.newArrayList();
			for (StepImplementation match : matches) {
				String detail = getDetail(match);
				details.add(detail);
			}
			return details;
		}

		protected String getDetail(StepImplementation match) {
			Method method = match.getMethod();
			Annotation[] annotations = method.getAnnotations();
			String joinedAnnotations = Joiner.on("\n\t").join(annotations);
			return String.format("%s\nwith annotations\n\t%s", method, joinedAnnotations);
		}
	}
}
