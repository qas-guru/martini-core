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

package guru.qas.martini.runtime.event.json;

import java.lang.reflect.Type;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import guru.qas.martini.gherkin.FeatureWrapper;

@SuppressWarnings({"unused", "WeakerAccess"})
public class DefaultFeatureSerializer implements FeatureSerializer {

	protected static final String KEY = "feature";
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFeatureSerializer.class);

	@Override
	public JsonElement serialize(FeatureWrapper feature, Type type, JsonSerializationContext context) {
		JsonElement content = new Builder(feature).build();
		return serialize(content);
	}

	protected JsonObject serialize(JsonElement content) {
		JsonObject serialized = new JsonObject();
		serialized.add(KEY, content);
		return serialized;
	}

	@SuppressWarnings("WeakerAccess")
	protected class Builder {

		protected final JsonObject serialized;
		protected final FeatureWrapper feature;

		protected Builder(FeatureWrapper feature) {
			this.serialized = new JsonObject();
			this.feature = feature;
		}

		protected JsonElement build() {
			setId();
			setName();
			setDescription();
			setLocation();
			return serialized;
		}

		protected void setId() {
			UUID id = feature.getId();
			String serializedId = id.toString();
			serialized.addProperty("id", serializedId);
		}

		protected void setName() {
			String name = feature.getName();
			serialized.addProperty("name", name);
		}

		protected void setDescription() {
			String description = feature.getDescription();
			serialized.addProperty("description", description);
		}

		protected void setLocation() {
			Resource resource = feature.getResource();
			String serializedResource = null == resource ? null : resource.toString();
			serialized.addProperty("location", serializedResource);
		}
	}
}
