package guru.qas.martini.feature;

import java.io.IOException;

import org.springframework.core.io.Resource;

public interface FeatureResourceLoader {
	Resource[] getResources() throws IOException;
}
