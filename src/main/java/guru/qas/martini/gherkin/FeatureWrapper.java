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

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;

import static com.google.common.base.Preconditions.checkNotNull;

public class FeatureWrapper {

	private final UUID id;
	private final Resource resource;
	private final Feature feature;

	public UUID getId() {
		return id;
	}

	public Resource getResource() {
		return resource;
	}

	public Feature getFeature() {
		return feature;
	}

	public FeatureWrapper(Feature feature, Resource resource) {
		this(UUID.randomUUID(), feature, resource);
	}

	public FeatureWrapper(UUID id, Feature feature, Resource resource) {
		this.id = checkNotNull(id, "null UUID");
		this.feature = checkNotNull(feature, "null Feature");
		this.resource = checkNotNull(resource, "null Resource");
	}

	public List<ScenarioDefinition> getChildren() {
		return feature.getChildren();
	}

	public String getName() {
		return feature.getName();
	}

	public String getDescription() {
		return feature.getDescription();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("resource", resource)
			.add("feature", feature)
			.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof FeatureWrapper)) {
			return false;
		}
		FeatureWrapper that = (FeatureWrapper) o;
		return Objects.equal(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
