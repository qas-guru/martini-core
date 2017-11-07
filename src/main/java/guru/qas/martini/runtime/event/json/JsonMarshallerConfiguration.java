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

package guru.qas.martini.runtime.event.json;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("WeakerAccess")
@Configuration
@Lazy
public class JsonMarshallerConfiguration implements BeanFactoryAware {

	protected AutowireCapableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		checkState(AutowireCapableBeanFactory.class.isInstance(beanFactory),
			"BeanFactory must be of type AutowireCapableBeanFactory but found %s", beanFactory.getClass());
		this.beanFactory = AutowireCapableBeanFactory.class.cast(beanFactory);
	}

	@Bean
	MartiniResultSerializer getMartiniResultSerializer(
		@Value("${json.martini.result.serializer.impl:#{null}}") Class<? extends MartiniResultSerializer> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultMartiniResultSerializer.class) :
			beanFactory.createBean(impl);
	}

	@Bean
	SuiteIdentifierSerializer getSuiteIdentifierSerializer(
		@Value("${json.suite.identifier.serializer.impl:#{null}}") Class<? extends SuiteIdentifierSerializer> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultSuiteIdentifierSerializer.class) :
			beanFactory.createBean(impl);
	}

	@Bean
	HostSerializer getHostSerializer(
		@Value("${json.suite.identifier.host.serializer.impl:#{null}}") Class<? extends HostSerializer> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultHostSerializer.class) :
			beanFactory.createBean(impl);
	}

	@Bean
	FeatureSerializer getFeatureSerializer(
		@Value("${json.feature.serializer.impl:#{null}}") Class<? extends FeatureSerializer> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultFeatureSerializer.class) :
			beanFactory.createBean(impl);
	}
}
