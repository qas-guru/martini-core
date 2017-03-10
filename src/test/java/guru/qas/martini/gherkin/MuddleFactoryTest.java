package guru.qas.martini.gherkin;

import java.io.IOException;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

import static com.google.common.base.Preconditions.checkState;

public class MuddleFactoryTest {

	@Test
	public void testBartender() throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		MuddleFactory factory = context.getBean(MuddleFactory.class);
		List<Muddle> muddles = factory.getMuddles();
		checkState(!muddles.isEmpty(), "no Muddles created");
	}
}
