package guru.qas.martini.test;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

import guru.qas.martini.Bartender;

public class MartiniTest {

	@Test
	public void testSomething() throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext(
			"classpath*:**/martiniContext.xml", "/applicationContext.xml");

		Bartender application = context.getBean(Bartender.class);
		application.doSomething();
	}
}
