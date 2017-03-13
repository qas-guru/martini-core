/*
Copyright 2017 Penny Rohr Curich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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
