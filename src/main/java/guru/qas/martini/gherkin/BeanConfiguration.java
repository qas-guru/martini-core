package guru.qas.martini.gherkin;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static com.google.common.base.Preconditions.checkState;

@Configuration
@Lazy
class BeanConfiguration implements BeanFactoryAware {

	private AutowireCapableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		checkState(AutowireCapableBeanFactory.class.isInstance(beanFactory),
			"BeanFactory must be of type AutowireCapableBeanFactory but found %s", beanFactory.getClass());
		this.beanFactory = AutowireCapableBeanFactory.class.cast(beanFactory);
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
