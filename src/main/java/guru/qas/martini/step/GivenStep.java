package guru.qas.martini.step;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public interface GivenStep {

	Pattern getPattern();

	Method getMethod();
}
