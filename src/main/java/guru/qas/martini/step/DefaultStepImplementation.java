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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.collect.Ordering;

import gherkin.ast.Step;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class DefaultStepImplementation implements StepImplementation {

	protected final String keyword;
	protected final Pattern pattern;
	protected final Method method;

	@Override
	public String getKeyword() {
		return keyword;
	}

	@Override
	public Optional<Pattern> getPattern() {
		return Optional.ofNullable(pattern);
	}

	@Override
	public Optional<Method> getMethod() {
		return Optional.ofNullable(method);
	}

	public DefaultStepImplementation(String keyword) {
		this(checkNotNull(keyword, "null String"), null, null);
	}

	public DefaultStepImplementation(String keyword, @Nullable Pattern pattern, @Nullable Method method) {
		this.keyword = checkNotNull(keyword, "null String");
		this.pattern = pattern;
		this.method = method;
	}

	@Override
	public boolean isMatch(Step step) {
		return null != method && isKeywordMatch(step) && isTextMatch(step);
	}

	protected boolean isKeywordMatch(Step that) {
		String normalizedThat = getNormalizedKeyword(that);
		int comparison = Ordering.natural().nullsFirst().compare(keyword, normalizedThat);
		return 0 == comparison;
	}

	protected String getNormalizedKeyword(Step step) {
		String keyword = step.getKeyword();
		return null == keyword ? null : keyword.trim();
	}

	protected boolean isTextMatch(Step step) {
		AtomicBoolean evaluation = new AtomicBoolean(false);
		getPattern().ifPresent(pattern -> {
			String text = step.getText();
			Matcher matcher = pattern.matcher(text);
			evaluation.set(isMatch(matcher));
		});
		return evaluation.get();
	}

	protected boolean isMatch(Matcher matcher) {
		boolean evaluation = false;
		if (null != method && matcher.find()) {
			int groups = matcher.groupCount();
			int parameterCount = method.getParameterCount();
			evaluation = groups == parameterCount;
		}
		return evaluation;
	}
}
