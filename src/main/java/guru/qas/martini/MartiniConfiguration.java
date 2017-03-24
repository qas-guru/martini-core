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

package guru.qas.martini;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.AbstractEnvironment;

import static com.google.common.base.Preconditions.*;

@Configuration
class MartiniConfiguration implements BeanFactoryAware {

	private AutowireCapableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		checkState(AutowireCapableBeanFactory.class.isInstance(beanFactory),
			"BeanFactory must be of type AutowireCapableBeanFactory but found %s", beanFactory.getClass());
		this.beanFactory = AutowireCapableBeanFactory.class.cast(beanFactory);
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
	@SuppressWarnings("unused") // Required by runtime engines.
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
}
