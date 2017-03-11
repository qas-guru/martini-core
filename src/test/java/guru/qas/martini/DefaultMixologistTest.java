package guru.qas.martini;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

import gherkin.ast.Feature;
import gherkin.pickles.Pickle;
import guru.qas.martini.gherkin.Recipe;

import static com.google.common.base.Preconditions.checkState;
import static org.testng.Assert.assertEquals;

public class DefaultMixologistTest {

	@Test
	public void testGetMartinis() throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		DefaultMixologist mixologist = context.getBean(DefaultMixologist.class);
		ImmutableList<Martini> martinis = mixologist.getMartinis();

		Martini match = null;
		for (Martini martini : martinis) {
			Recipe recipe = martini.getRecipe();
			Feature feature = recipe.getFeature();
			String featureName = feature.getName().trim();
			if ("Functionality of the Reporting Subsystem".equals(featureName)) {
				Pickle pickle = recipe.getPickle();
				String scenarioName = pickle.getName().trim();
				if ("A Corner Case".equals(scenarioName)) {
					checkState(null == match,
						"more than one Martini found for sample.feature:A Corner Case");
					match = martini;
				}
			}
		}

		checkState(null != match, "no Martini loaded for sample.feature:A Corner Case");
		String toString = match.toString();

		Resource resource = new ClassPathResource("/subsystem/sample.feature");
		String path = resource.getURL().toExternalForm();

		String expected = "Feature: Functionality of the Reporting Subsystem\n" +
			"Resource: " + path + "\n" +
			"Scenario: A Corner Case\n" +
			"Line: 10";

		assertEquals(toString, expected, "wrong information returned through toString()");
	}
}
