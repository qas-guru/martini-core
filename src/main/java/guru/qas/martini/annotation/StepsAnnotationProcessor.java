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

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.ImmutableList;
import static com.google.common.base.Preconditions.*;

@Component
@Lazy
class StepsAnnotationProcessor implements BeanPostProcessor, ApplicationContextAware {

	private ApplicationContext context;
	private ImmutableList<ReflectionUtils.MethodCallback> callbacks;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
		AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
		checkState(ConfigurableListableBeanFactory.class.isInstance(beanFactory),
			"Martini requires the use of a ConfigurableListableBeanFactory");
		ConfigurableListableBeanFactory configurable = ConfigurableListableBeanFactory.class.cast(beanFactory);
		callbacks = ImmutableList.<ReflectionUtils.MethodCallback>builder()
			.add(new MartiniAnnotationCallback<>(Given.class, GivenContainer.class, configurable))
			.add(new MartiniAnnotationCallback<>(And.class, AndContainer.class, configurable))
			.add(new MartiniAnnotationCallback<>(When.class, WhenContainer.class, configurable))
			.add(new MartiniAnnotationCallback<>(Then.class, ThenContainer.class, configurable))
			.build();
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		try {
			Class<?> wrapped = AopUtils.getTargetClass(bean);
			if (!isSpring(wrapped)) {
				Class<?> declaring = AnnotationUtils.findAnnotationDeclaringClass(Steps.class, wrapped);
				if (null != declaring) {
					processStepsBean(beanName, wrapped);
				}
			}
			return bean;
		}
		catch (Exception e) {
			throw new FatalBeanException("unable to processAnnotationContainer @Steps beans", e);
		}
	}

	private boolean isSpring(Class c) {
		String name = c.getName();
		return name.startsWith("org.spring");
	}

	private void processStepsBean(String beanName, Class wrapped) {
		checkState(context.isSingleton(beanName), "Beans annotated @Steps must have singleton scope.");
		for (ReflectionUtils.MethodCallback callback : callbacks) {
			ReflectionUtils.doWithMethods(wrapped, callback);
		}
	}
}
