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

package guru.qas.martini.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import guru.qas.martini.step.DefaultStep;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class GivenCallback extends AbstractAnnotationCallback {

	protected GivenCallback(ConfigurableListableBeanFactory beanFactory) {
		super(beanFactory);
	}

	protected void processAnnotation(Method method) {
		Given annotation = AnnotationUtils.findAnnotation(method, Given.class);
		if (null != annotation) {
			process(method, annotation);
		}
	}

	protected void process(Method method, Given annotation) {
		checkState(Modifier.isPublic(method.getModifiers()), "Method is not public: %s", method);
		String regex = annotation.value().trim();
		checkState(!regex.isEmpty(), "@Given requires non-empty regex values.");
		checkState(!regularExpressions.contains(regex), "Multiple methods found for @Given regex \"%s\"", regex);
		Pattern pattern = Pattern.compile(regex);
		regularExpressions.add(regex);

		Class<? extends Annotation> annotationClass = annotation.annotationType();
		String keyword = annotationClass.getSimpleName();
		String name = String.format("%s%s", keyword.toLowerCase(), atomicInteger.getAndIncrement());

		DefaultStep step = new DefaultStep(keyword, pattern, method);
		beanFactory.registerSingleton(name, step);
	}

	protected void processAnnotationContainer(Method method) {
		GivenContainer container = AnnotationUtils.findAnnotation(method, GivenContainer.class);
		if (null != container) {
			Given[] annotations = container.value();
			for (Given annotation : annotations) {
				process(method, annotation);
			}
		}
	}
}
