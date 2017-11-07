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
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import gherkin.ast.Feature;
import guru.qas.martini.gherkin.Recipe;

@SuppressWarnings("unused")
public class DefaultFeatureSerializer implements FeatureSerializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFeatureSerializer.class);

	@Override
	public JsonElement serialize(Recipe recipe, Type type, JsonSerializationContext context) {
		return new Builder(recipe).build();
	}

	@SuppressWarnings("WeakerAccess")
	protected class Builder {

		protected final JsonObject serialized;
		protected final Recipe recipe;

		protected Builder(Recipe recipe) {
			this.serialized = new JsonObject();
			this.recipe = recipe;
		}

		protected JsonElement build() {
			Feature feature = recipe.getFeature();
			setName(feature);
			setDescription(feature);
			setLocation();
			return serialized;
		}

		protected void setName(Feature feature) {
			String name = feature.getName();
			serialized.addProperty("name", name);
		}

		protected void setDescription(Feature feature) {
			String description = feature.getDescription();
			serialized.addProperty("description", description);
		}

		protected void setLocation() {
			URL location = getURL();
			String path = null == location ? null : location.getPath();
			serialized.addProperty("location", path);
		}

		protected URL getURL() {
			Resource resource = recipe.getSource();
			return getURL(resource);
		}

		protected URL getURL(Resource resource) {
			URL url = null;
			try {
				url = null == resource ? null : resource.getURL();
			}
			catch (Exception e) {
				LOGGER.warn("unable to parse URL from resource {}", resource);
			}
			return url;
		}
	}
}
