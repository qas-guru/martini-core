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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gherkin.ast.Step;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class DefaultGivenStep implements GivenStep {

	protected final static String KEYWORD = "Given";

	protected final Pattern pattern;
	protected final Method method;

	public DefaultGivenStep(Pattern pattern, Method method) {
		this.pattern = checkNotNull(pattern, "null Pattern");
		this.method = checkNotNull(method, "null Method");
	}

	@Override
	public Pattern getPattern() {
		return pattern;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public boolean isMatch(Step step) {
		boolean evaluation = false;
		if (isKeywordMatch(step)) {
			String text = step.getText();
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				int groups = matcher.groupCount();
				int parameterCount = method.getParameterCount();
				evaluation = groups == parameterCount;
			}
		}
		return evaluation;
	}

	protected boolean isKeywordMatch(Step step) {
		String keyword = step.getKeyword();
		String trimmed = keyword.trim();
		return KEYWORD.equals(trimmed);
	}
}
