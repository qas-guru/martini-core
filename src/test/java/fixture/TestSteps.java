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

package fixture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.Steps;
import guru.qas.martini.gherkin.Mixology;

@SuppressWarnings("unused") // Referenced by Martini.
@Steps
public class TestSteps {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSteps.class);

	@SuppressWarnings("FieldCanBeLocal")
	private final Mixology mixology;

	@Autowired
	TestSteps(Mixology mixology) {
		this.mixology = mixology;
	}

	@Given("^a pre-existing condition$")
	@Given("^a given$")
	public void aGiven() {
	}

	@Given("^another \"(.+)\" here$")
	public void anotherStep(String myParameter) {
		LOGGER.info("executing with parameter {}", myParameter);
	}

	private void aPrivateMethod() {
	}
}
