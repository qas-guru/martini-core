package guru.qas.martini.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import guru.qas.martini.FeatureResourceLoader;

@Configuration
@Lazy
public class FeatureResourceLoaderConfiguration {

	@Bean
	FeatureResourceLoader getFeatureResourceLoader(
		AutowireCapableBeanFactory beanFactory,
		@Value("${feature.resource.loader.implementation}") Class<? extends FeatureResourceLoader> implementation
	) throws IllegalAccessException, InstantiationException {
		return beanFactory.createBean(implementation);
	}
}
