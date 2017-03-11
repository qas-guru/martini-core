package guru.qas.martini;

import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;

@SuppressWarnings("WeakerAccess")
public class DefaultMartini implements Martini {

	protected final Recipe recipe;
	protected final gherkin.ast.Step step;
	protected final StepImplementation implementation;

	protected DefaultMartini(Recipe recipe, gherkin.ast.Step step, StepImplementation implementation) {
		this.recipe = recipe;
		this.step = step;
		this.implementation = implementation;
	}
}
