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
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import fixture.ParameterizedTestSteps;
import gherkin.ast.Feature;
import gherkin.ast.Step;
import gherkin.pickles.Pickle;
import guru.qas.martini.gherkin.MartiniTag;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static org.testng.Assert.*;

@SuppressWarnings("WeakerAccess")
public class DefaultMixologistTest {

	protected ClassPathXmlApplicationContext context;
	protected DefaultMixologist mixologist;

	@BeforeClass
	public void setUpClass() {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		mixologist = context.getBean(DefaultMixologist.class);
	}

	@AfterClass
	public void tearDownClass() {
		mixologist = null;
		if (null != context) {
			context.close();
		}
		context = null;
	}

	@Test
	public void testGetMartinis() throws IOException {
		Martini match = getMartini("Functionality of the Reporting Subsystem", "A Corner Case");

		checkState(null != match, "no Martini loaded for sample.feature:A Corner Case");

		String expected = "Functionality_of_the_Reporting_Subsystem:A_Corner_Case:25";
		assertEquals(match.getId(), expected, "wrong ID returned");
		assertEquals(match.toString(), expected, "toString() not returning ID");
	}

	protected Martini getMartini(String featureName, String scenarioName) {
		Martini match = null;
		ImmutableList<Martini> martinis = mixologist.getMartinis();
		for (Martini martini : martinis) {
			Recipe recipe = martini.getRecipe();
			Feature feature = recipe.getFeature();
			String fName = feature.getName().trim();
			if (featureName.equals(fName)) {
				Pickle pickle = recipe.getPickle();
				String sName = pickle.getName().trim();
				if (scenarioName.equals(sName)) {
					checkState(null == match,
						"more than one Martini found for %s:%s", featureName, scenarioName);
					match = martini;
				}
			}
		}
		return match;
	}

	@SuppressWarnings("Guava")
	@Test
	public void testGetParameterizedMartini() throws NoSuchMethodException {
		Martini martini = getMartini("Parameterized Method Calls", "A Parameterized Case");
		Map<Step, StepImplementation> stepIndex = martini.getStepIndex();
		Collection<StepImplementation> implementations = stepIndex.values();
		ImmutableList<StepImplementation> filtered = FluentIterable.from(implementations).filter(notNull()).toList();
		assertFalse(filtered.isEmpty(), "expected at least one implementation");
		StepImplementation givenImplementation = filtered.get(0);

		Pattern pattern = givenImplementation.getPattern();
		assertNotNull(pattern, "expected implementation to have a Pattern");

		String regex = pattern.pattern();
		assertEquals(regex, "^a pre-existing condition known as \"(.+)\"$", "Pattern contains wrong regex");

		Method method = givenImplementation.getMethod();
		Method expected = ParameterizedTestSteps.class.getDeclaredMethod("aPreExistingConditionKnownAs", String.class);
		assertEquals(method, expected, "implementation contains wrong Method");
	}

	@DataProvider(name = "inactionableFilterArgumentDataProvider")
	public Object[][] getInactionableFilterArguments() {
		return new Object[][]{
			new Object[]{null},
			new Object[]{""},
			new Object[]{" "},
			new Object[]{" \t \n"},
		};
	}

	@Test(dataProvider = "inactionableFilterArgumentDataProvider")
	public void testGetFilteredMartinisWithInactionableArgument(String argument) {
		Collection<Martini> martinis = mixologist.getMartinis(argument);
		Collection<Martini> allMartinis = mixologist.getMartinis();
		assertEquals(martinis, allMartinis,
			"all martinis should have been returned for inactionable argument " + argument);
	}

	@Test
	public void testGetFilteredMartinis() {
		String filter = "isSmoke()";
		Collection<Martini> martinis = mixologist.getMartinis(filter);
		checkState(!martinis.isEmpty(), "no martinis returned for actionable filter " + filter);
		for (Martini martini : martinis) {
			Collection<MartiniTag> tags = martini.getTags();
			boolean foundTag = false;
			for (Iterator<MartiniTag> i = tags.iterator(); !foundTag && i.hasNext(); ) {
				MartiniTag tag = i.next();
				String name = tag.getName();
				foundTag = name.equals("Smoke");
			}
			checkState(foundTag, "Martini returned not matching tag Smoke");
		}
	}

	@Test
	public void testGetComplexSPeL() {
		String filter = "getTags('@Meta').?[getArgument()?.equals('Selenium')].size() > 0";
		Collection<Martini> martinis = mixologist.getMartinis(filter);
		checkState(!martinis.isEmpty(), "no matching Martinis found");
	}

	@Test
	public void testGetCustomSPeL() {
		String filter = "getTags('@Meta').containsArgument('Selenium')";
		Collection<Martini> martinis = mixologist.getMartinis(filter);
		checkState(!martinis.isEmpty(), "no matching Martinis found");
	}

	@Test
	public void testNegativeGetCustomSPeL() {
		String filter = "getTags('@Meta').containsArgument('Bogus')";
		Collection<Martini> martinis = mixologist.getMartinis(filter);
		checkState(martinis.isEmpty(), "no matching Martinis should have been returned");
	}
}
