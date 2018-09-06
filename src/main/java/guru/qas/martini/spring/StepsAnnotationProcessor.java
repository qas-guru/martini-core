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

package guru.qas.martini.spring;

import javax.annotation.Nonnull;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.ImmutableList;

import guru.qas.martini.annotation.And;
import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.MartiniAnnotationCallback;
import guru.qas.martini.annotation.Steps;
import guru.qas.martini.annotation.Then;
import guru.qas.martini.annotation.When;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
@Component
@Lazy
public class StepsAnnotationProcessor implements BeanPostProcessor, ApplicationContextAware, InitializingBean {

	private ApplicationContext applicationContext;
	private ImmutableList<ReflectionUtils.MethodCallback> callbacks;

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = checkNotNull(applicationContext, "null ApplicationContext");
	}

	@Override
	public void afterPropertiesSet() {
		callbacks = ImmutableList.of(
			getCallback(Given.class),
			getCallback(And.class),
			getCallback(When.class),
			getCallback(Then.class));
	}

	protected <Keyword> MartiniAnnotationCallback getCallback(Class<? extends Keyword> keywordAnnotation) {
		AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
		checkState(DefaultListableBeanFactory.class.isInstance(beanFactory),
			"expected BeanFactory to be an instance of DefaultListableBeanFactory");

		DefaultListableBeanFactory factory = DefaultListableBeanFactory.class.cast(beanFactory);
		String beanName = String.format("%s.callback", keywordAnnotation.getName());

		BeanDefinition beanDefinition = getBeanDefinition(keywordAnnotation);

		factory.registerBeanDefinition(beanName, beanDefinition);
		return beanFactory.getBean(beanName, MartiniAnnotationCallback.class);
	}

	protected <Keyword> BeanDefinition getBeanDefinition(Class<? extends Keyword> keyword) {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		beanDefinition.setBeanClass(MartiniAnnotationCallback.class);
		beanDefinition.setDependencyCheck(AbstractBeanDefinition.DEPENDENCY_CHECK_ALL);

		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		constructorArgumentValues.addGenericArgumentValue(keyword);
		beanDefinition.setConstructorArgumentValues(constructorArgumentValues);
		return beanDefinition;
	}

	@Override
	public Object postProcessBeforeInitialization(@Nonnull Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(@Nonnull Object bean, String beanName) throws BeansException {
		try {
			Class<?> wrapped = AopUtils.getTargetClass(bean);
			if (!isSpring(wrapped)) {
				Class<?> declaring = AnnotationUtils.findAnnotationDeclaringClass(Steps.class, wrapped);
				if (null != declaring) {
					Steps annotation = applicationContext.findAnnotationOnBean(beanName, Steps.class);
					Lazy lazy = applicationContext.findAnnotationOnBean(beanName, Lazy.class);
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
		checkState(applicationContext.isSingleton(beanName), "Beans annotated @Steps must have singleton scope.");
		for (ReflectionUtils.MethodCallback callback : callbacks) {
			ReflectionUtils.doWithMethods(wrapped, callback);
		}
	}
}
