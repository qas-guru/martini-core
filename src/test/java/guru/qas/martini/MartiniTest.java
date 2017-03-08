package guru.qas.martini;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

public class MartiniTest {

	@Test
	public void testSomething() throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/guru/martini/applicationContext.xml");
		Martini application = context.getBean(Martini.class);
		application.doSomething();
	}
}
