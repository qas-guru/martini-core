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
