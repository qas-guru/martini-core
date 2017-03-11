package guru.qas.martini.step;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class DefaultGivenStep implements GivenStep {

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
}
