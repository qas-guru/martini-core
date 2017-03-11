package guru.qas.martini.annotation;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
@Component
public class StepsAnnotationProcessor implements BeanPostProcessor, ApplicationContextAware {

	protected ApplicationContext context;
	protected GivenCallback givenCallback;

	protected GivenCallback getGivenCallback() {
		return givenCallback;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
		this.givenCallback = new GivenCallback();
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		try {
			Class<?> wrapped = AopUtils.getTargetClass(bean);
			if (!isSpring(wrapped)) {
				Class<?> declaring = AnnotationUtils.findAnnotationDeclaringClass(Steps.class, wrapped);
				if (null != declaring) {
					processStepsBean(beanName, wrapped);
				}
			}
			return bean;
		}
		catch (Exception e) {
			throw new FatalBeanException("unable to process @Steps beans", e);
		}
	}

	protected boolean isSpring(Class c) {
		String name = c.getCanonicalName();
		return name.startsWith("org.spring");
	}

	protected void processStepsBean(String beanName, Class wrapped) {
		checkState(context.isSingleton(beanName), "Beans annotated @Steps must have singleton scope.");
		ReflectionUtils.doWithMethods(wrapped, givenCallback);
	}
}
