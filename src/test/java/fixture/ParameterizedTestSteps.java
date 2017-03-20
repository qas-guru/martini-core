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

import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.Steps;

@SuppressWarnings("WeakerAccess")
@Steps
public class ParameterizedTestSteps {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ParameterizedTestSteps.class);

	@Given("^a pre-existing condition without arguments$")
	public void aPreExistingConditionWithoutArguments() throws Throwable {
		LOGGER.info("executing aPreExistingWithoutArguments");
	}

	@Given("^a pre-existing condition known as \"(.+)\"$")
	public void aPreExistingConditionKnownAs(String condition) throws Throwable {
		LOGGER.info("executing aPreExistingConditionKnownAs(String)");
	}
}
