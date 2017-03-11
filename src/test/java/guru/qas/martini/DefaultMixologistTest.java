package guru.qas.martini;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

public class DefaultMixologistTest {

	@Test
	public void testDoSomething() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		DefaultMixologist mixologist = context.getBean(DefaultMixologist.class);
		mixologist.doSomething();
	}
}
