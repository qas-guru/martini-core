/*
Copyright 2017-2018 Penny Rohr Curich

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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import fixture.ParameterizedTestSteps;
import gherkin.ast.Step;
import guru.qas.martini.gate.MartiniGate;
import guru.qas.martini.step.UnimplementedStep;
import guru.qas.martini.step.StepImplementation;
import guru.qas.martini.tag.MartiniTag;

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
	public void testGetMartinis() {
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
		ImmutableList<StepImplementation> filtered = FluentIterable.from(implementations)
			.filter(notNull())
			.filter(Predicates.not(Predicates.instanceOf(UnimplementedStep.class)))
			.toList();
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

	@Test
	public void testBackground() {
		String id = "Fairytales:Sleeping_Beauty:22";
		Martini martini = this.getMartini(id);
		checkState(null != martini, "unable to retrieve Martini by ID %s", id);

		Map<Step, StepImplementation> index = martini.getStepIndex();
		checkState(4 == index.size(), "wrong number of steps returned, expected 4 but found %s", index.size());
	}

	@Test
	public void testGetByFeature() {
		String feature = "Parameterized Method Calls";
		ImmutableList<Martini> martinis = mixologist.getMartinis();
		List<Martini> expected = martinis.stream()
			.filter(m -> m.getFeatureName().equals(feature))
			.collect(Collectors.toList());

		String filter = String.format("isFeature('%s ')", feature); // Note the errant space.
		List<Martini> actual = new ArrayList<>(mixologist.getMartinis(filter));

		checkState(actual.equals(expected), "wrong Martinis returned, expected %s but found %s",
			Joiner.on(", ").join(expected),
			Joiner.on(", ").join(actual));
	}

	@Test
	public void testGetByScenario() {
		String scenario = "A Non-Parameterized Case";
		ImmutableList<Martini> martinis = mixologist.getMartinis();
		List<Martini> expected = martinis.stream()
			.filter(m -> m.getScenarioName().equals(scenario))
			.collect(Collectors.toList());

		String filter = String.format("isScenario('%s')", scenario);
		List<Martini> actual = new ArrayList<>(mixologist.getMartinis(filter));

		checkState(actual.equals(expected), "wrong Martinis returned, expected %s but found %s",
			Joiner.on(", ").join(expected),
			Joiner.on(", ").join(actual));
	}

	@Test
	public void testGetById() {
		String id = "Parameterized_Method_Calls:A_Non-Parameterized_Case:19";
		ImmutableList<Martini> martinis = mixologist.getMartinis();
		List<Martini> expected = martinis.stream()
			.filter(m -> m.getId().equals(id))
			.collect(Collectors.toList());

		String filter = String.format("isId('%s')", id);
		List<Martini> actual = new ArrayList<>(mixologist.getMartinis(filter));

		checkState(actual.equals(expected), "wrong Martinis returned, expected %s but found %s",
			Joiner.on(", ").join(expected),
			Joiner.on(", ").join(actual));
	}

	@Test
	public void testGates() {
		Martini martini = mixologist.getMartinis().stream()
			.filter(m -> "Multiple Intersecting Gates".equals(m.getScenarioName()))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("unable to find Scenario"));

		Collection<MartiniGate> gates = martini.getGates();
		int actual = gates.size();
		checkState(5 == actual, "wrong number of gates; expected 5 but found %s", actual);
	}
}
