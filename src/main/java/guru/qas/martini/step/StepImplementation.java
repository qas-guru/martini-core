package guru.qas.martini.step;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import gherkin.ast.Step;

public interface StepImplementation {

	StepImplementation UNIMPLEMENTED = new StepImplementation() {
	};

	default Pattern getPattern() {
		throw new UnsupportedOperationException();
	}

	default Method getMethod() {
		throw new UnsupportedOperationException();
	}

	default boolean isMatch(Step recipe) {
		throw new UnsupportedOperationException();
	}
}
