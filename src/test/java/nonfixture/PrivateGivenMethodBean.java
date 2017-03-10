package nonfixture;

import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.Steps;

@Steps
public class PrivateGivenMethodBean {

	@Given("the method is not accessible")
	private void doSomethingNotAccessible() {
	}
}
