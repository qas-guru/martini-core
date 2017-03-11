package guru.qas.martini.gherkin;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultGherkinResourceLoader implements GherkinResourceLoader {

	protected static final String RESOURCE_PATTERN = "classpath*:**/*.feature";

	protected final ResourceLoader loader;

	@Autowired
	protected DefaultGherkinResourceLoader(ResourceLoader loader) {
		this.loader = loader;
	}

	@Override
	public Resource[] getFeatureResources() throws IOException {
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
		return resolver.getResources(RESOURCE_PATTERN);
	}
}
