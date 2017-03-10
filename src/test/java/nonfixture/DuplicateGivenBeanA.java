package nonfixture;

import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.Steps;

@Steps
public class DuplicateGivenBeanA {
	@Given("a duplicate step")
	public void duplicateStepA() {
	}
}
