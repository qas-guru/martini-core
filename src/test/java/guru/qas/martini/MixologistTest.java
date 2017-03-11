package guru.qas.martini;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

public class MixologistTest {

	@Test
	public void testDoSomething() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		DefaultMixologist bartender = context.getBean(DefaultMixologist.class);
		bartender.doSomething();
	}
}
