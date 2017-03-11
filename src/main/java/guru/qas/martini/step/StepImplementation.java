package guru.qas.martini.step;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import gherkin.ast.Step;

public interface StepImplementation {

	Pattern getPattern();

	Method getMethod();

	boolean isMatch(Step recipe);
}
