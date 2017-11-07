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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.http.HttpEntity;

import com.google.common.base.Throwables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import gherkin.ast.Location;
import gherkin.ast.Step;
import guru.qas.martini.event.Status;
import guru.qas.martini.result.StepResult;
import guru.qas.martini.step.StepImplementation;

public class DefaultStepResultSerializer implements StepResultSerializer {

	@Override
	public JsonElement serialize(StepResult stepResult, Type type, JsonSerializationContext context) {
		return new Builder(stepResult, context).build();
	}

	@SuppressWarnings("WeakerAccess")
	protected class Builder {

		protected final JsonObject serialized;
		protected final StepResult stepResult;
		protected final JsonSerializationContext context;

		protected Builder(StepResult stepResult, JsonSerializationContext context) {
			this.context = context;
			this.serialized = new JsonObject();
			this.stepResult = stepResult;
		}

		protected JsonObject build() {
			Step step = stepResult.getStep();
			addStartTimestamp();
			addEndTimestamp();
			addKeyword(step);
			addText(step);
			addLocation(step);
			addMethod();
			addStatus();
			addEmbedded();
			addException();
			return serialized;
		}

		protected void addStartTimestamp() {
			Long startTimestamp = stepResult.getStartTimestamp();
			serialized.addProperty("startTimestamp", startTimestamp);
		}

		protected void addEndTimestamp() {
			Long endTimestamp = stepResult.getEndTimestamp();
			serialized.addProperty("endTimestamp", endTimestamp);
		}

		protected void addKeyword(Step step) {
			String keyword = step.getKeyword();
			serialized.addProperty("keyword", keyword);
		}

		protected void addText(Step step) {
			String text = step.getText();
			serialized.addProperty("text", text);
		}

		protected void addLocation(Step step) {
			Location location = step.getLocation();
			int line = location.getLine();
			serialized.addProperty("line", line);
		}

		protected void addMethod() {
			StepImplementation implementation = stepResult.getStepImplementation();
			Method method = implementation.getMethod();
			JsonElement serializedMethod = null == method ?
				null : context.serialize(implementation, StepImplementation.class);
			serialized.add("method", serializedMethod);
		}

		protected void addStatus() {
			Status status = stepResult.getStatus();
			String serializedStatus = status.name();
			serialized.addProperty("status", serializedStatus);
		}

		protected void addEmbedded() {
			List<HttpEntity> embedded = stepResult.getEmbedded();
			JsonElement serializedEmbedded = context.serialize(embedded);
			serialized.add("embedded", serializedEmbedded);
		}

		protected void addException() {
			Exception exception = stepResult.getException();
			String serializedException = null == exception ? null : Throwables.getStackTraceAsString(exception);
			serialized.addProperty("exception", serializedException);
		}
	}
}
