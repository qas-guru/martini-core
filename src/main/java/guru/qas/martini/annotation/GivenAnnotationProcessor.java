package guru.qas.martini.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
class GivenAnnotationProcessor implements BeanPostProcessor {

	private ConfigurableListableBeanFactory beanFActory;

	@Autowired
	GivenAnnotationProcessor(ConfigurableListableBeanFactory beanFactory) {
		this.beanFActory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		this.scanDataAccessAnnotation(bean, beanName);
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	void scanDataAccessAnnotation(Object bean, String beanName) {
		this.configureInjection(bean);
	}

	private void configureInjection(Object bean) {
		Class<?> managedBeanClass = bean.getClass();
		ReflectionUtils.MethodCallback callback =
			new GivenCallback(beanFActory, bean);
		ReflectionUtils.doWithMethods(managedBeanClass, callback);
	}
}
