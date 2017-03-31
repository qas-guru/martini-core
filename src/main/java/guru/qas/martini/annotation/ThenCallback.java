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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import guru.qas.martini.step.DefaultStep;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class ThenCallback extends AbstractAnnotationCallback {

	protected ThenCallback(ConfigurableListableBeanFactory beanFactory) {
		super(beanFactory);
	}

	protected void processAnnotation(Method method) {
		Then annotation = AnnotationUtils.findAnnotation(method, Then.class);
		if (null != annotation) {
			process(method, annotation);
		}
	}

	protected void process(Method method, Then annotation) {
		checkState(Modifier.isPublic(method.getModifiers()), "Method is not public: %s", method);
		String regex = annotation.value().trim();
		checkState(!regex.isEmpty(), "@Then requires non-empty regex values.");
		checkState(!regularExpressions.contains(regex), "Multiple methods found for @Then regex \"%s\"", regex);
		Pattern pattern = Pattern.compile(regex);
		regularExpressions.add(regex);

		String name = String.format("then%s", atomicInteger.getAndIncrement());

		DefaultStep step = new DefaultStep("Then", pattern, method);
		beanFactory.registerSingleton(name, step);
	}

	protected void processAnnotationContainer(Method method) {
		ThenContainer container = AnnotationUtils.findAnnotation(method, ThenContainer.class);
		if (null != container) {
			Then[] annotations = container.value();
			for (Then annotation : annotations) {
				process(method, annotation);
			}
		}
	}
}
