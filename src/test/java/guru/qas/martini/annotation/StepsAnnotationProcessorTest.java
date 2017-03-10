package guru.qas.martini.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

import failures.DuplicateBeanA;
import failures.DuplicateBeanB;
import fixture.TestSteps;

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
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("emptyContext.xml");
		ConfigurableListableBeanFactory factory = context.getBeanFactory();

		DuplicateBeanA beanA = new DuplicateBeanA();
		factory.registerSingleton(DuplicateBeanA.class.getName(), beanA);

		DuplicateBeanB beanB = new DuplicateBeanB();
		factory.registerSingleton(DuplicateBeanB.class.getName(), beanB);

		StepsAnnotationProcessor processor = context.getBean(StepsAnnotationProcessor.class);
		processor.postProcessAfterInitialization(beanA, DuplicateBeanA.class.getName());
		processor.postProcessAfterInitialization(beanB, DuplicateBeanB.class.getName());
	}
}
