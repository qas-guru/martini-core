package guru.qas.martini.gherkin;

import java.io.IOException;

import org.springframework.core.io.Resource;

public interface Mixology {

	Iterable<Recipe> get(Resource resource) throws IOException;
}
