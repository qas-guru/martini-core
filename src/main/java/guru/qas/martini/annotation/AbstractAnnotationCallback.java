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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractAnnotationCallback implements ReflectionUtils.MethodCallback {

	protected final ConfigurableListableBeanFactory beanFactory;
	protected final AtomicInteger atomicInteger;
	protected final Set<String> regularExpressions;

	protected AbstractAnnotationCallback(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.atomicInteger = new AtomicInteger();
		this.regularExpressions = Sets.newHashSet();
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		checkNotNull(method, "null Method");
		processAnnotation(method);
		processAnnotationContainer(method);
	}

	protected abstract void processAnnotation(Method method);

	protected abstract void processAnnotationContainer(Method method);
}
