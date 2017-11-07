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
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import guru.qas.martini.event.SuiteIdentifier;

@SuppressWarnings("WeakerAccess")
public class DefaultSuiteIdentifierSerializer implements SuiteIdentifierSerializer {

	protected final ConcurrentMap<String, JsonObject> cache;

	public DefaultSuiteIdentifierSerializer() {
		this.cache = new ConcurrentHashMap<>();
	}

	@Override
	public JsonElement serialize(SuiteIdentifier identifier, Type type, JsonSerializationContext context) {
		String id = identifier.getId();
		JsonObject serialized = cache.get(id);

		if (null == serialized) {
			serialized = new Builder(identifier, context).build();
			serialized = null == cache.putIfAbsent(id, serialized) ? serialized : cache.get(id);
		}

		return serialized;
	}

	protected class Builder {

		protected final SuiteIdentifier identifier;
		protected final JsonSerializationContext context;
		protected final JsonObject serialized;

		public Builder(SuiteIdentifier identifier, JsonSerializationContext context) {
			this.context = context;
			this.serialized = new JsonObject();
			this.identifier = identifier;
		}

		protected JsonObject build() {
			setId();
			setTimestamp();
			setName();
			setHost();
			setProfiles();
			setEnvironment();
			return serialized;
		}

		protected void setId() {
			String id = identifier.getId();
			serialized.addProperty("id", id);
		}

		protected void setTimestamp() {
			Long timestamp = identifier.getStartTimestamp();
			serialized.addProperty("startTimestamp", timestamp);
		}

		protected void setName() {
			String name = identifier.getName();
			serialized.addProperty("name", name);
		}

		protected void setHost() {
			JsonElement host = context.serialize(identifier, NetworkInterface.class);
			serialized.add("host", host);
		}

		protected void setProfiles() {
			Collection<String> profiles = identifier.getProfiles();
			JsonElement serializedProfiles = context.serialize(profiles, Collection.class);
			serialized.add("profiles", serializedProfiles);
		}

		protected void setEnvironment() {
			Map<String, String> environment = identifier.getEnvironmentVariables();
			JsonElement serializedEnvironment = context.serialize(environment, Map.class);
			serialized.add("environment", serializedEnvironment);
		}
	}
}
