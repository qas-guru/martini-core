package guru.qas.martini.parser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy
class ParserConfiguration {

	@Bean
	FeatureResourceLoader getFeatureResourceLoader(
		AutowireCapableBeanFactory beanFactory,
		@Value("${feature.resource.loader.implementation}") Class<? extends FeatureResourceLoader> implementation
	) throws IllegalAccessException, InstantiationException {
		return beanFactory.createBean(implementation);
	}

	@Bean
	ScenarioParser getScenarioParser(
		AutowireCapableBeanFactory beanFactory,
		@Value("${scenario.parser.implementation}") Class<? extends ScenarioParser> implementation
	) {
		return beanFactory.createBean(implementation);
	}
}
