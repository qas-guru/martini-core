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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import guru.qas.martini.step.StepImplementation;

public class DefaultStepImplementationSerializer implements StepImplementationSerializer {

	@Override
	public JsonElement serialize(StepImplementation implementation, Type type, JsonSerializationContext context) {
		return new Builder(implementation, context).build();
	}

	@SuppressWarnings("WeakerAccess")
	protected class Builder {

		protected final JsonObject serialized;
		protected final StepImplementation implementation;
		protected final JsonSerializationContext context;

		public Builder(StepImplementation implementation, JsonSerializationContext context) {
			this.context = context;
			this.serialized = new JsonObject();
			this.implementation = implementation;
		}

		protected JsonObject build() {
			setClass();
			setPattern();

			implementation.getMethod().ifPresent(method -> {
				setName(method);
				setParameters(method);
			});

			return serialized;
		}

		protected void setClass() {
			Class<? extends StepImplementation> clazz = implementation.getClass();
			String serializedClass = clazz.getName();
			serialized.addProperty("class", serializedClass);
		}

		protected void setName(Method method) {
			String serializedMethod = method.getName();
			serialized.addProperty("name", serializedMethod);
		}

		protected void setParameters(Method method) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			setParameters(parameterTypes);
		}

		protected void setParameters(Class[] parameterTypeArray) {
			List<String> parameterTypes = Lists.newArrayListWithExpectedSize(parameterTypeArray.length);
			for (Class parameterType : parameterTypeArray) {
				String name = parameterType.getName();
				parameterTypes.add(name);
			}
			setParameters(parameterTypes);
		}

		protected void setParameters(List<String> parameterTypes) {
			JsonElement serializedParameterTypes = context.serialize(parameterTypes, List.class);
			serialized.add("parameters", serializedParameterTypes);
		}

		protected void setPattern() {
			implementation.getPattern().ifPresent(pattern -> {
				String serializedPattern = pattern.pattern();
				serialized.addProperty("pattern", serializedPattern);
			});
		}
	}
}
