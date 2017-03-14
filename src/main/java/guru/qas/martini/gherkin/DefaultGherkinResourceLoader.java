/*
Copyright 2017 Penny Rohr Curich

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Default implementation of a GherkinResourceLoader.
 */
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
