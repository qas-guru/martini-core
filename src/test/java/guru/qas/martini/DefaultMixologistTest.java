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
import gherkin.ast.Step;
import guru.qas.martini.tag.MartiniTag;
import guru.qas.martini.step.StepImplementation;

import static com.google.common.base.Preconditions.*;
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
		String id = "Functionality_of_the_Reporting_Subsystem:A_Corner_Case:25";
		Martini match = getMartini(id);
		checkState(null != match, "no Martini found matching ID [%s]", id);
	}

	protected Martini getMartini(String expected) {
		Martini match = null;
		ImmutableList<Martini> martinis = mixologist.getMartinis();
		for (Iterator<Martini> i = martinis.iterator(); null == match && i.hasNext(); ) {
			Martini candidate = i.next();
			String id = candidate.getId();
			match = expected.equals(id) ? candidate : null;
		}
		return match;
	}

	@SuppressWarnings("Guava")
	@Test
	public void testGetParameterizedMartini() throws NoSuchMethodException {
		String id = "Parameterized_Method_Calls:A_Parameterized_Case:25";
		Martini martini = getMartini(id);
		checkState(null != martini, "no Martini found matching ID [%s]", id);

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
		checkState(!martinis.isEmpty(), "no martinis returned for actionable tag " + filter);
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
	public void testGetCustomSPeL() {
		String id = "Functionality_of_the_Reporting_Subsystem:A_Corner_Case:25";
		Martini expected = getMartini(id);
		checkNotNull(expected, "no Martini found matching ID [%s]", id);

		String filter = "isMeta('Selenium')";
		Collection<Martini> martinis = mixologist.getMartinis(filter);
		assertTrue(martinis.contains(expected), "expected Martini not returned");
	}

	@Test
	public void testNegativeGetCustomSPeL() {
		String filter = "isMeta('Bogus')";
		Collection<Martini> martinis = mixologist.getMartinis(filter);
		assertTrue(martinis.isEmpty(), "no matching Martinis should have been returned");
	}

	@Test
	public void testGetByClassification() {
		String id = "Functionality_of_the_Reporting_Subsystem:A_Corner_Case:25";
		Martini expected = this.getMartini(id);
		checkNotNull(expected, "no Martini found matching ID [%s]", id);

		String filter = "isCategory('AdHoc')";
		Collection<Martini> martinis = mixologist.getMartinis(filter);
		assertTrue(martinis.contains(expected), "expected Martini not returned");
	}

	@Test
	public void testGetByClassificationHierarchy() {
		String id = "Functionality_of_the_Reporting_Subsystem:A_Corner_Case:25";
		Martini expected = this.getMartini(id);

		String filter = "isCategory('Core')";
		Collection<Martini> martinis = mixologist.getMartinis(filter);
		assertTrue(martinis.contains(expected), "expected Martini not returned");
	}
}
