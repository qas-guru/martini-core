package guru.qas.martini.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class GivenCallback implements ReflectionUtils.MethodCallback {

	final Map<String, Pattern> patternIndex;
	final Map<Pattern, Method> methodIndex;

	protected Map<String, Pattern> getPatternIndex() {
		return ImmutableMap.copyOf(patternIndex);
	}

	protected Map<Pattern, Method> getMethodIndex() {
		return ImmutableMap.copyOf(methodIndex);
	}

	protected GivenCallback() {
		patternIndex = Maps.newHashMap();
		methodIndex = Maps.newHashMap();
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		checkNotNull(method, "null Method");
		processAnnotation(method);
		processContainerAnnotation(method);
	}

	protected void processAnnotation(Method method) {
		Given annotation = AnnotationUtils.findAnnotation(method, Given.class);
		if (null != annotation) {
			doWithAnnotation(method, annotation);
		}
	}

	protected void doWithAnnotation(Method method, Given annotation) {
		checkState(Modifier.isPublic(method.getModifiers()), "Method is not public: %s", method);
		String regex = annotation.value().trim();
		checkState(!regex.isEmpty(), "@Given requires non-empty regex values.");
		checkState(!patternIndex.containsKey(regex), "Multiple methods found for @Given regex \"%s\"", regex);
		Pattern pattern = Pattern.compile(regex);
		patternIndex.put(regex, pattern);
		methodIndex.put(pattern, method);
	}

	protected void processContainerAnnotation(Method method) {
		GivenContainer container = AnnotationUtils.findAnnotation(method, GivenContainer.class);
		if (null != container) {
			Given[] annotations = container.value();
			for (Given annotation : annotations) {
				doWithAnnotation(method, annotation);
			}
		}
	}
}
