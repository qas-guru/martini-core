package failures;

import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.Steps;

@Steps
public class DuplicateBeanA {
	@Given("a duplicate step")
	public void duplicateStepA() {
	}
}
