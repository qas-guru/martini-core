package guru.qas.martini.feature;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

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
	private final AtomicReference<Resource[]> reference;

	@Autowired
	DefaultFeatureResourceLoader(ResourceLoader loader) {
		this.loader = loader;
		reference = new AtomicReference<>();
	}

	@Override
	public Resource[] getResources() throws IOException {
		synchronized (reference) {
			Resource[] resources = reference.get();
			if (null == resources) {
				ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
				resources = resolver.getResources(RESOURCE_PATTERN);
				reference.set(resources);
			}
			return resources;
		}
	}
}
