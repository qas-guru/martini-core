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

package guru.qas.martini.spring.configuration;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import guru.qas.martini.DefaultMartiniFactory;
import guru.qas.martini.DefaultMixologist;
import guru.qas.martini.MartiniFactory;
import guru.qas.martini.Mixologist;
import guru.qas.martini.event.DefaultMartiniEventPublisher;
import guru.qas.martini.event.MartiniEventPublisher;
import guru.qas.martini.gate.DefaultMartiniGateFactory;
import guru.qas.martini.gate.MartiniGateFactory;
import guru.qas.martini.scope.DefaultMartiniScenarioScope;

import guru.qas.martini.scope.MartiniScenarioScope;
import guru.qas.martini.tag.Categories;
import guru.qas.martini.tag.DefaultCategories;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
@Configuration
@Lazy
class MartiniConfiguration implements ApplicationContextAware, EnvironmentAware {

	private Environment environment;
	private AutowireCapableBeanFactory beanFactory;

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
		checkNotNull(applicationContext, "null ApplicationContext");
		beanFactory = applicationContext.getAutowireCapableBeanFactory();
	}

	@Override
	public void setEnvironment(@Nonnull Environment environment) {
		checkNotNull(environment, "null Environment");
		this.environment = environment;
	}

	@Bean
	ConversionService getConversionService(ConfigurableEnvironment environment) {
		return environment.getConversionService();
	}

	@Bean
	MartiniGateFactory getMartiniGateFactory() {
		return getOverride(MartiniGateFactory.IMPLEMENTATION_KEY, MartiniGateFactory.class)
			.orElse(beanFactory.createBean(DefaultMartiniGateFactory.class));
	}

	@Bean
	MartiniFactory getMartiniFactory() {
		return getOverride(MartiniFactory.IMPLEMENTATION_KEY, MartiniFactory.class)
			.orElse(beanFactory.createBean(DefaultMartiniFactory.class));
	}

	@Bean
	Mixologist getMixologist() {
		return getOverride(Mixologist.IMPLEMENTATION_KEY, Mixologist.class)
			.orElse(beanFactory.createBean(DefaultMixologist.class));
	}

	@Bean
	MartiniScenarioScope getMartiniScenarioScope(ConfigurableBeanFactory beanFactory) {
		MartiniScenarioScope scope =
			getOverride(MartiniScenarioScope.IMPLEMENTATION_KEY, MartiniScenarioScope.class)
				.orElse(this.beanFactory.createBean(DefaultMartiniScenarioScope.class));
		beanFactory.registerScope(MartiniScenarioScope.NAME, scope);
		return scope;
	}

	@Bean
	public Categories getCategories() {
		return getOverride(Categories.IMPLEMENTATION_KEY, Categories.class)
			.orElse(beanFactory.createBean(DefaultCategories.class));
	}

	@Bean
	public MartiniEventPublisher getMartiniEventPublisher() {
		return getOverride(MartiniEventPublisher.IMPLEMENTATION_KEY, MartiniEventPublisher.class)
			.orElse(beanFactory.createBean(DefaultMartiniEventPublisher.class));
	}

	protected <T> Optional<T> getOverride(String key, Class<T> expectedType) {
		Class<?> implementation = environment.getProperty(key, Class.class);
		Object o = null == implementation ? null : beanFactory.createBean(implementation);
		checkState(null == o || expectedType.isInstance(o),
			"unexpected instance type for bean %s; expected %s but got %s", key, expectedType, null == o ? null : o.getClass());
		return null == o ? Optional.empty() : Optional.of(expectedType.cast(o));
	}
}
