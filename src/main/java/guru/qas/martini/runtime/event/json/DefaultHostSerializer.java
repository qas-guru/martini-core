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

package guru.qas.martini.runtime.event.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import guru.qas.martini.event.SuiteIdentifier;

@SuppressWarnings({"unused", "WeakerAccess"})
public class DefaultHostSerializer implements HostSerializer {

	@Override
	public JsonElement serialize(SuiteIdentifier identifier, Type type, JsonSerializationContext context) {
		JsonObject serialized = new JsonObject();
		addHostName(identifier, serialized);
		addHostAddress(identifier, serialized);
		addUsername(identifier, serialized);
		return serialized;
	}

	protected void addHostName(SuiteIdentifier identifier, JsonObject serialized) {
		String hostname = identifier.getHostName().orElse(null);
		serialized.addProperty("hostName", hostname);
	}

	protected void addHostAddress(SuiteIdentifier identifier, JsonObject serialized) {
		String ip = identifier.getHostAddress().orElse(null);
		serialized.addProperty("hostAddress", ip);
	}

	protected void addUsername(SuiteIdentifier identifier, JsonObject serialized) {
		String username = identifier.getUsername().orElse(null);
		serialized.addProperty("username", username);
	}
}
