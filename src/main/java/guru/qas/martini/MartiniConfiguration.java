package guru.qas.martini;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
@Configuration
@Lazy
public class MartiniConfiguration implements BeanFactoryAware {

	protected AutowireCapableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		checkState(AutowireCapableBeanFactory.class.isInstance(beanFactory),
			"BeanFactory must be of type AutowireCapableBeanFactory but found %s", beanFactory.getClass());
		this.beanFactory = AutowireCapableBeanFactory.class.cast(beanFactory);
	}

	@Bean
	protected Mixologist getMixologist(
		@Value("${gherkin.mixologist:#{null}") Class<? extends Mixologist> impl
	) {
		return null == impl ?
			beanFactory.createBean(DefaultMixologist.class) :
			beanFactory.createBean(impl);
	}
}
