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

package guru.qas.martini.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Sets;

import guru.qas.martini.MartiniException;
import guru.qas.martini.i18n.MessageSources;
import guru.qas.martini.step.DefaultStep;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class MartiniAnnotationCallback<A extends Annotation, C extends Annotation> implements ReflectionUtils.MethodCallback {

	protected static final String REGEX_PATTERN_METHOD = "value";
	protected static final String KEY_ERROR_MESSAGE = "martini.annotation.exception";

	protected final Class<A> annotationClass;
	protected final Class<C> annotationContainerClass;
	protected final ConfigurableListableBeanFactory beanFactory;
	protected final AtomicInteger atomicInteger;
	protected final Set<String> regularExpressions;

	protected MartiniAnnotationCallback(
		Class<A> annotationClass,
		Class<C> annotationContainerClass,
		ConfigurableListableBeanFactory beanFactory
	) {
		this.annotationClass = annotationClass;
		this.annotationContainerClass = annotationContainerClass;
		this.beanFactory = beanFactory;
		this.atomicInteger = new AtomicInteger();
		this.regularExpressions = Sets.newHashSet();
	}

	@Override
	public void doWith(@Nonnull Method method) throws IllegalArgumentException {
		checkNotNull(method, "null Method");
		processAnnotation(method);
		processAnnotationContainer(method);
	}

	protected void processAnnotation(Method method) {
		A annotation = AnnotationUtils.findAnnotation(method, annotationClass);
		if (null != annotation) {
			process(method, annotation);
		}
	}

	protected void process(Method method, A annotation) {
		checkState(Modifier.isPublic(method.getModifiers()), "Method is not public: %s", method);

		String regex = getValue(annotation).trim();
		String annotationName = annotationClass.getSimpleName();
		checkState(!regex.isEmpty(), "@%s requires non-empty regex values.", annotationName);
		checkState(!regularExpressions.contains(regex), "Multiple methods found for @%s regex \"%s\"", annotationName, regex);
		Pattern pattern = Pattern.compile(regex);
		regularExpressions.add(regex);

		String name = String.format("%s%s", annotationName.toLowerCase(), atomicInteger.getAndIncrement());

		DefaultStep step = new DefaultStep(annotationName, pattern, method);
		beanFactory.registerSingleton(name, step);
	}

	protected String getValue(A annotation) {
		try {
			Method valueMethod = annotationClass.getMethod(REGEX_PATTERN_METHOD);
			Object value = valueMethod.invoke(annotation);
			return String.class.cast(value);
		}
		catch (Exception e) {
			throw getMartiniException(annotation, e);
		}
	}

	protected MartiniException getMartiniException(Annotation annotation, Exception cause) {
		MessageSource messageSource = MessageSources.getMessageSource(MartiniAnnotationCallback.class);
		throw new MartiniException.Builder()
			.setCause(cause)
			.setMessageSource(messageSource)
			.setKey(KEY_ERROR_MESSAGE)
			.setArguments(annotation)
			.build();
	}

	protected void processAnnotationContainer(Method method) {
		C container = AnnotationUtils.findAnnotation(method, annotationContainerClass);
		if (null != container) {
			A[] annotations = getValues(container);
			for (A annotation : annotations) {
				process(method, annotation);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected A[] getValues(C annotation) {
		try {
			Method valueMethod = annotationContainerClass.getMethod(REGEX_PATTERN_METHOD);
			Object value = valueMethod.invoke(annotation);
			return (A[]) value;
		}
		catch (Exception e) {
			throw getMartiniException(annotation, e);
		}
	}
}