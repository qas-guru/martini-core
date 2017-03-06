package guru.qas.martini;

import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import com.google.common.collect.Lists;

@Configurable
public class DefaultFeatureResourceLoader implements InitializingBean, FeatureResourceLoader {

	private static final String PATTERN = "classpath*:*.feature";


	private final ResourceLoader resourceLoader;
	private ResourcePatternResolver resolver;

	@Autowired
	protected DefaultFeatureResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
	}

	@Override
	public Iterable<Resource> getResources() throws IOException {
		Resource[] resources = resolver.getResources(PATTERN);
		return Lists.newArrayList(resources);
	}
}
