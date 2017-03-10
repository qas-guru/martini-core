package guru.qas.martini;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

public class BartenderTest {

	@Test
	public void testDoSomething() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		Bartender bartender = context.getBean(Bartender.class);
		bartender.doSomething();
	}
}
