/*
Copyright 2017-2018 Penny Rohr Curich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package guru.qas.martini.gherkin;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Default implementation of a GherkinResourceLoader.
 */
@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultGherkinResourceLoader implements GherkinResourceLoader {

	protected static final String DEFAULT_PATTERN = "classpath*:**/*.feature";

	protected final ResourceLoader loader;
	protected final List<String> patterns;

	@Autowired
	protected DefaultGherkinResourceLoader(
		ResourceLoader loader,
		@Value("${martini.feature.resources:#{null}}") String[] patterns
	) {
		this.loader = loader;
		this.patterns = null == patterns ? ImmutableList.of(DEFAULT_PATTERN) : ImmutableList.copyOf(patterns);
	}

	@Override
	public Resource[] getFeatureResources() {
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
		Set<Resource> resources = new HashSet<>();
		patterns.forEach(p -> {
			try {
				Resource[] resourceArray = resolver.getResources(p);
				resources.addAll(Lists.newArrayList(resourceArray));
			}
			catch (IOException e) {
				throw new RuntimeException("unable to load Resource for pattern " + p, e);
			}
		});
		return resources.toArray(new Resource[resources.size()]);
	}
}