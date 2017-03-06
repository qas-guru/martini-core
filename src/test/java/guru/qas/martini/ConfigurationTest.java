package guru.qas.martini;

import java.io.IOException;
import java.util.Collection;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.*;


public class ConfigurationTest {

	@Test
	public void testDefaultConfiguration() throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/guru/martini/applicationContext.xml");
		Martini application = context.getBean(Martini.class);
		Collection<Resource> featureResources = application.getFeatureResources();
		assertThat(!featureResources.isEmpty()).withFailMessage("no features loaded");
	}
}
