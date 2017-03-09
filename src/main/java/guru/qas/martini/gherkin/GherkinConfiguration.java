package guru.qas.martini.gherkin;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static com.google.common.base.Preconditions.checkState;

@Configuration
@Lazy
public class GherkinConfiguration implements BeanFactoryAware {

	private AutowireCapableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		checkState(AutowireCapableBeanFactory.class.isInstance(beanFactory),
			"BeanFactory must be of type AutowireCapableBeanFactory but found %s", beanFactory.getClass());
		this.beanFactory = AutowireCapableBeanFactory.class.cast(beanFactory);

	}

	@Bean
	FeatureResourceLoader getFeatureResourceLoader(
		@Value("${gherkin.feature.resource.loader:#{null}}") Class<? extends FeatureResourceLoader> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultFeatureResourceLoader.class) :
			beanFactory.createBean(impl);
	}

	@Bean
	MuddleFactory getMuddleFactory(
		@Value("${martini.muddle.factory:#{null}}") Class<? extends MuddleFactory> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultMuddleFactory.class) : beanFactory.createBean(impl);
	}
}
