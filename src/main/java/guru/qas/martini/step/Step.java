package guru.qas.martini.step;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import guru.qas.martini.gherkin.Recipe;

public interface Step {

	Pattern getPattern();

	Method getMethod();

	boolean isMatch(Recipe recipe);
}
