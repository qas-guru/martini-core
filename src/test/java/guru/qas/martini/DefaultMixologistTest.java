package guru.qas.martini;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

public class DefaultMixologistTest {

	@Test
	public void testGetMartinis() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		DefaultMixologist mixologist = context.getBean(DefaultMixologist.class);
		ImmutableList<Martini> martinis = mixologist.getMartinis();
		System.out.println(martinis.get(0));
	}
}
