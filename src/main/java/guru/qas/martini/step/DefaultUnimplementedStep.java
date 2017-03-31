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

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import gherkin.ast.Step;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class DefaultUnimplementedStep implements StepImplementation {

	protected final String keyword;
	protected final String text;

	@Override
	public String getKeyword() {
		return keyword;
	}

	public String getText() {
		return text;
	}

	protected DefaultUnimplementedStep(String keyword, String text) {
		this.keyword = keyword;
		this.text = text;
	}

	@Override
	public Pattern getPattern() {
		return null;
	}

	@Override
	public Method getMethod() {
		return null;
	}

	@Override
	public boolean isMatch(Step step) {
		return false;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected Builder() {
		}

		public DefaultUnimplementedStep build(Step step) {
			checkNotNull(step, "null Step");
			String keyword = getKeyword(step);
			String text = step.getText();
			return new DefaultUnimplementedStep(keyword, text);
		}

		protected String getKeyword(Step step) {
			String keyword = step.getKeyword();
			return null == keyword ? null : keyword.trim();
		}
	}
}
