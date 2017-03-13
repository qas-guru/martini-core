package fixture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import guru.qas.martini.annotation.Given;
import guru.qas.martini.annotation.Steps;
import guru.qas.martini.gherkin.Mixology;

@Steps
public class TestSteps {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSteps.class);

	@SuppressWarnings("FieldCanBeLocal")
	private final Mixology mixology;

	@Autowired
	TestSteps(Mixology mixology) {
		this.mixology = mixology;
	}

	@Given("a pre-existing condition")
	@Given("a given")
	public void aGiven() {
	}

	@Given("another \"(.+)\" here")
	public void anotherStep(String myParameter) {
		LOGGER.info("executing with parameter {}", myParameter);
	}

	private void aPrivateMethod() {
	}
}
