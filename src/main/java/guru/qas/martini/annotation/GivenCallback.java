package guru.qas.martini.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkState;

final class GivenCallback implements ReflectionUtils.MethodCallback {

	private Map<String, Pattern> patternIndex = Maps.newHashMap();
	private Map<Pattern, Method> methodIndex = Maps.newHashMap();

	Map<String, Pattern> getPatternIndex() {
		return patternIndex;
	}

	Map<Pattern, Method> getMethodIndex() {
		return methodIndex;
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		Given annotation = AnnotationUtils.findAnnotation(method, Given.class);
		if (null != annotation) {
			doWith(method, annotation);
		}
	}

	private void doWith(Method method, Given annotation) {
		checkState(Modifier.isPublic(method.getModifiers()), "Method is not public: %s", method);
		String regex = annotation.value().trim();
		checkState(!regex.isEmpty(), "@Given requires a non-empty regex value.");
		checkState(!patternIndex.containsKey(regex), "Multiple methods found for @Given annotation \"%s\"", regex);
		Pattern pattern = Pattern.compile(regex);
		patternIndex.put(regex, pattern);
		methodIndex.put(pattern, method);
	}
}
