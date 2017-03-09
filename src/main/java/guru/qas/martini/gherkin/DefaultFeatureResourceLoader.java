package guru.qas.martini.gherkin;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configurable
public class DefaultFeatureResourceLoader implements FeatureResourceLoader {

	private static final String RESOURCE_PATTERN = "classpath*:**/*.feature";

	private final ResourceLoader loader;

	@Autowired
	DefaultFeatureResourceLoader(ResourceLoader loader) {
		this.loader = loader;
	}

	@Override
	public Resource[] getResources() throws IOException {
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
		return resolver.getResources(RESOURCE_PATTERN);
	}
}
