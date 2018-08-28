/*
Copyright 2018 Penny Rohr Curich

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

package fixture.gate;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Bean;

import guru.qas.martini.Martini;
import guru.qas.martini.annotation.And;
import guru.qas.martini.annotation.Gated;
import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.ScenarioScoped;
import guru.qas.martini.annotation.Steps;
import guru.qas.martini.annotation.Then;
import guru.qas.martini.annotation.When;
import guru.qas.martini.gate.MartiniGate;
import guru.qas.martini.result.MartiniResult;
import guru.qas.martini.scope.MartiniScenarioScope;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("unused")
@Steps
@Gated(name = "One")
@Gated(name = "Two")
public class ClassGateOneTwoSteps {

	@ScenarioScoped
	@Bean("gateCount")
	AtomicInteger getGateCount() {
		return new AtomicInteger(0);
	}

	private final MartiniScenarioScope scope;
	private final AtomicInteger gateCount;

	public ClassGateOneTwoSteps(MartiniScenarioScope scope, AtomicInteger gateCount) {
		this.scope = scope;
		this.gateCount = gateCount;
	}

	@Given("^a step class having gates \"One\" and \"Two\"$")
	public void given() {
	}

	@Gated(name = "Four")
	@And("^a method having gate \"Four\"$")
	public void andGateFour() {
	}

	@Gated(name = "One")
	@Gated(name = "Five")
	@And("^a method having gates \"One\" and \"Five\"$")
	public void andGatesOneAndFive() {
	}

	@When("^gates are retrieved$")
	public void getGates() {
		MartiniResult martiniResult = scope.getMartiniResult()
			.orElseThrow(() -> new IllegalStateException("not within scenario scope"));
		Martini martini = martiniResult.getMartini();
		Collection<MartiniGate> gates = martini.getGates();
		gateCount.set(gates.size());
	}

	@Then("^there are \"(.)\" gates$")
	public void assertGateCount(Integer expected) {
		int actual = gateCount.get();
		checkState(expected.equals(actual), "wrong number of gates; expected %s got %s", expected, actual);
	}
}