package guru.qas.martini.feature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy
public class FeatureResourceLoaderConfiguration {

	@Bean
	FeatureResourceLoader getFeatureResourceLoader(
		AutowireCapableBeanFactory beanFactory,
		@Value("${feature.resource.loader.impl:guru.qas.martini.feature.DefaultFeatureResourceLoader}") Class<? extends FeatureResourceLoader> implementation
	) {
		return beanFactory.createBean(implementation);
	}
}
