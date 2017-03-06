package guru.qas.martini;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableSet;

@Service
public class Martini {

	protected final FeatureResourceLoader loader;
	private ImmutableSet<Resource> featureResources;

	@Autowired
	protected Martini(FeatureResourceLoader loader) {
		this.loader = loader;
	}

	protected synchronized Collection<Resource> getFeatureResources() throws IOException {
		if (null == featureResources) {
			Iterable<Resource> resources = loader.getResources();
			featureResources = ImmutableSet.copyOf(resources);
		}
		return featureResources;
	}

	public void doSomething() {
		System.out.println("hello");
	}
}
