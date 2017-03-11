package nonfixture;

import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.Steps;

@Steps
public class MultipleGivenBean {

	@Given("this is regular expression one")
	@Given("this is regular expression two")
	public void doSomething(){
	}
}
