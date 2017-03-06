package guru.qas.martini.parser;

import java.io.IOException;

import org.springframework.core.io.Resource;

public interface FeatureResourceLoader {
	Iterable<Resource> getResources() throws IOException;
}