package guru.qas.martini.gherkin;

import java.io.IOException;

import org.springframework.core.io.Resource;

public interface GherkinResourceLoader {
	Resource[] getFeatureResources() throws IOException;
}
