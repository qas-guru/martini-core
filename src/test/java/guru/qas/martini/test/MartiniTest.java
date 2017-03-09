package guru.qas.martini.test;

import java.io.IOException;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

import fixture.TestSteps;
import guru.qas.martini.Bartender;
import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.Steps;

import static com.google.common.base.Preconditions.*;

public class MartiniTest {

	private ApplicationContext context;

	@BeforeClass
	public void setUpClass() {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
	}

	@AfterClass
	public void tearDownClass() {
		context = null;
	}

	@Test
	public void testBartender() throws IOException {
		Bartender application = context.getBean(Bartender.class);
		application.doSomething();
	}

	@Test
	public void testStepsScanning() {
		String[] names = context.getBeanNamesForAnnotation(Steps.class);
		checkNotNull(names, "no @Steps classes found");

		String expectedName = "testSteps";
		Set<String> asSet = Sets.newHashSet(names);
		checkState(asSet.contains("testSteps"), "did not find bean name %s in annotated @Steps components", expectedName);

		Object o = context.getBean(expectedName);
		checkState(TestSteps.class.isInstance(o), "bean returned for name %s is not of expected type", expectedName);
	}

	@Test
	public void testGivenScanning() {
		String[] names = context.getBeanNamesForAnnotation(Given.class);
		checkNotNull(names, "no @Given methods found");

//		String expectedName = "testSteps";
//		Set<String> asSet = Sets.newHashSet(names);
//		checkState(asSet.contains("testSteps"), "did not find bean name %s in annotated @Steps components", expectedName);
//
//		Object o = context.getBean(expectedName);
//		checkState(TestSteps.class.isInstance(o), "bean returned for name %s is not of expected type", expectedName);
	}
}
