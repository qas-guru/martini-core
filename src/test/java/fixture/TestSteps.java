package fixture;

import org.springframework.beans.factory.annotation.Autowired;

import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.Steps;
import guru.qas.martini.gherkin.MuddleFactory;

@Steps
public class TestSteps {

	@Autowired
	TestSteps(MuddleFactory muddleFactory) {
		System.out.println("breakpoint");
	}

	@Given("a given")
	public void aGiven() {
	}

	@Given("another \"(.+)\" here")
	public void anotherStep(String myParameter) {
		System.out.println("myParameter");
	}

	private void aPrivateMethod() {
	}
}
