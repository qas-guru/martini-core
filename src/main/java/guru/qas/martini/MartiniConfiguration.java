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

package guru.qas.martini;

import javax.annotation.Nonnull;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.AbstractEnvironment;

import guru.qas.martini.event.DefaultMartiniEventPublisher;
import guru.qas.martini.event.MartiniEventPublisher;
import guru.qas.martini.tag.Categories;
import guru.qas.martini.tag.DefaultCategories;
import guru.qas.martini.scope.ScenarioScope;

@Configuration
class MartiniConfiguration implements ApplicationContextAware {

	private AutowireCapableBeanFactory beanFactory;

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
		beanFactory = applicationContext.getAutowireCapableBeanFactory();
	}

	@Bean
	Mixologist getMixologist(
		@Value("${gherkin.mixologist:#{null}}") Class<? extends Mixologist> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultMixologist.class) :
			beanFactory.createBean(impl);
	}

	/**
	 * @param environment Spring Environment
	 * @param beanName    name of Conversion Service bean Martini should utilize
	 * @return specified, or Environment default, conversion service.
	 */
	@Lazy
	@Bean
	ConversionService getConversionService(
		AbstractEnvironment environment,
		@Value("${conversion.service.bean.name:#{null}}") String beanName
	) {
		return null == beanName ?
			environment.getConversionService() :
			beanFactory.getBean(beanName, ConversionService.class);
	}

	@Bean
	@Lazy
	public static CustomScopeConfigurer customScopeConfigurer(
		ScenarioScope scope
	) {
		CustomScopeConfigurer configurer = new CustomScopeConfigurer();
		configurer.addScope("scenario", scope);
		return configurer;
	}

	@Bean
	public Categories getCategories(
		@Value("${categories.implementation:#{null}}") Class<? extends Categories> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultCategories.class) :
			beanFactory.createBean(impl);
	}

	@Bean
	public MartiniEventPublisher getMartiniEventPublisher(
		@Value("${martini.event.publisher.implementation:#{null}}") Class<? extends MartiniEventPublisher> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultMartiniEventPublisher.class) :
			beanFactory.createBean(impl);
	}
}
