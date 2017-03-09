package guru.qas.martini.annotation;

import java.lang.reflect.Method;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

class GivenCallback implements ReflectionUtils.MethodCallback {

	private final ConfigurableListableBeanFactory beanFactory;
	private final Object bean;

	GivenCallback(ConfigurableListableBeanFactory bf, Object bean) {
		beanFactory = bf;
		this.bean = bean;
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		if (!method.isAnnotationPresent(Given.class)) {
			return;
		}
		System.out.println("breakpoint");
	}
}
