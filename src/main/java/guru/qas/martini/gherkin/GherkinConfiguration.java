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

package guru.qas.martini.gherkin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy
class GherkinConfiguration {

	private AutowireCapableBeanFactory beanFactory;

	@Autowired
	GherkinConfiguration(AutowireCapableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Bean
	GherkinResourceLoader getFeatureResourceLoader(
		@Value("${gherkin.resource.loader:#{null}}") Class<? extends GherkinResourceLoader> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultGherkinResourceLoader.class) :
			beanFactory.createBean(impl);
	}

	@Bean
	Mixology getMixology(
		@Value("${martini.mixology:#{null}}") Class<? extends Mixology> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultMixology.class) : beanFactory.createBean(impl);
	}
}
