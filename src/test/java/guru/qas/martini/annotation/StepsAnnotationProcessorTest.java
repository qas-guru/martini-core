package guru.qas.martini.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

import nonfixture.DuplicateGivenBeanA;
import nonfixture.DuplicateGivenBeanB;
import fixture.TestSteps;
import nonfixture.MultipleGivenBean;
import nonfixture.PrivateGivenMethodBean;

import static com.google.common.base.Preconditions.*;

public class StepsAnnotationProcessorTest {

	@Test
	public void testPostProcessAfterInitialization() throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		String[] beanNames = context.getBeanNamesForType(TestSteps.class);
		Object bean = context.getBean(beanNames[0]);

		StepsAnnotationProcessor processor = context.getBean(StepsAnnotationProcessor.class);
		GivenCallback callback = processor.getGivenCallback();
		Map<String, Pattern> patternIndex = callback.getPatternIndex();
		Pattern pattern = patternIndex.get("a given");
		checkNotNull(pattern, "regex 'a given' not found in GivenCallback");

		Map<Pattern, Method> methodIndex = callback.getMethodIndex();
		Method method = methodIndex.get(pattern);
		checkNotNull(method, "expected method not found in GivenCallback");
		Class<?> declaringClass = method.getDeclaringClass();
		checkState(declaringClass.equals(bean.getClass()), "method comes from incorrect class");
	}

	@Test(expectedExceptions = FatalBeanException.class)
	public void testDuplicateStep() {
		DuplicateGivenBeanA beanA = new DuplicateGivenBeanA();
		DuplicateGivenBeanB beanB = new DuplicateGivenBeanB();
		ClassPathXmlApplicationContext context = getContext(beanA, beanB);

		process(context, beanA);
		process(context, beanB);
	}

	private static ClassPathXmlApplicationContext getContext(Object... beans) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("emptyContext.xml");
		ConfigurableListableBeanFactory factory = context.getBeanFactory();
		for (Object bean : beans) {
			factory.registerSingleton(bean.getClass().getName(), bean);
		}
		return context;
	}

	private static void process(ClassPathXmlApplicationContext context, Object... beans) {
		StepsAnnotationProcessor processor = context.getBean(StepsAnnotationProcessor.class);
		for (Object bean : beans) {
			processor.postProcessAfterInitialization(bean, bean.getClass().getName());
		}
	}

	@Test
	public void testMultipleGivenRegex() {
		MultipleGivenBean bean = new MultipleGivenBean();
		ClassPathXmlApplicationContext context = getContext(bean);
		process(context, bean);

		StepsAnnotationProcessor processor = context.getBean(StepsAnnotationProcessor.class);
		GivenCallback givenCallback = processor.getGivenCallback();
		Map<String, Pattern> patternIndex = givenCallback.getPatternIndex();
		checkState(2 == patternIndex.size(),
			"expected two patterns for a single @Given having multiple strings values");
	}

	@Test(expectedExceptions = FatalBeanException.class)
	public void testInaccessibleMethod() {
		PrivateGivenMethodBean bean = new PrivateGivenMethodBean();
		ClassPathXmlApplicationContext context = getContext(bean);
		process(context, bean);
	}

	@Test(expectedExceptions = FatalBeanException.class)
	public void testNonSingletonSteps() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("emptyContext.xml");
		ConfigurableListableBeanFactory factory = context.getBeanFactory();

		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(DuplicateGivenBeanA.class);
		definition.setLazyInit(false);
		definition.setScope("prototype");

		BeanDefinitionRegistry registry = BeanDefinitionRegistry.class.cast(factory);
		registry.registerBeanDefinition(DuplicateGivenBeanA.class.getName(), definition);
		process(context, new DuplicateGivenBeanA());
	}
}
