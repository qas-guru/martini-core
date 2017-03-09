package guru.qas.martini.gherkin;

import java.io.IOException;

import org.springframework.core.io.Resource;

public interface FeatureResourceLoader {
	Resource[] getResources() throws IOException;
}
