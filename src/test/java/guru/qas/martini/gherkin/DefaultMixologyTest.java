package guru.qas.martini.gherkin;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

import static org.testng.Assert.assertEquals;

public class DefaultMixologyTest {

	@Test
	public void testMixology() throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		DefaultMixology factory = context.getBean(DefaultMixology.class);

		Resource resource = new ClassPathResource("/subsystem/sample.feature");
		Iterable<Recipe> recipes = factory.get(resource);

		int recipeCount = Iterables.size(recipes);
		assertEquals(1, recipeCount, "wrong number of Recipe objects returned");
	}
}
