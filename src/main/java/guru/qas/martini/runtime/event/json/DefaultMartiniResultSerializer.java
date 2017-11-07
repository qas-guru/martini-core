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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;

import gherkin.ast.Feature;
import guru.qas.martini.Martini;
import guru.qas.martini.event.Status;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.result.MartiniResult;
import guru.qas.martini.result.StepResult;
import guru.qas.martini.tag.Categories;
import guru.qas.martini.tag.MartiniTag;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultMartiniResultSerializer implements MartiniResultSerializer {

	protected final static String PROPERTY = "martini";

	private final Categories categories;

	@Autowired
	public DefaultMartiniResultSerializer(Categories categories) {
		this.categories = categories;
	}

	@Override
	public JsonElement serialize(MartiniResult result, Type type, JsonSerializationContext context) {
		JsonObject serialized = new JsonObject();
		JsonElement contents = new Builder(result, context).build();
		serialized.add(PROPERTY, contents);
		return serialized;
	}

	protected class Builder {

		protected final MartiniResult result;
		protected final JsonSerializationContext context;
		protected final JsonObject serialized;

		public Builder(MartiniResult result, JsonSerializationContext context) {
			this.result = result;
			this.context = context;
			serialized = new JsonObject();
		}

		JsonElement build() {
			setSuite();

			Martini martini = result.getMartini();
			setFeature(martini);

			setStartTimestamp();
			setEndTimestamp();
			setThreadGroup();
			setThread();

			setId(martini);
			setLine(martini);
			setName(martini);
			setCategories(martini);
			setTags(martini);

			setStatus();
			setSteps();

			return serialized;
		}

		protected void setSuite() {
			SuiteIdentifier identifier = result.getSuiteIdentifier();
			JsonElement serializedSuite = context.serialize(identifier, SuiteIdentifier.class);
			serialized.add("suite", serializedSuite);
		}

		protected void setFeature(Martini martini) {
			Recipe recipe = martini.getRecipe();
			JsonElement serializedFeature = context.serialize(recipe, Feature.class);
			serialized.add("feature", serializedFeature);
		}

		protected void setStartTimestamp() {
			Long timestamp = result.getStartTimestamp();
			serialized.addProperty("startTimestamp", timestamp);
		}

		protected void setEndTimestamp() {
			Long endTimestamp = result.getEndTimestamp();
			serialized.addProperty("endTimestamp", endTimestamp);
		}

		protected void setThreadGroup() {
			String threadGroup = result.getThreadGroupName();
			serialized.addProperty("threadGroup", threadGroup);
		}

		protected void setThread() {
			String thread = result.getThreadName();
			serialized.addProperty("thread", thread);
		}

		protected void setId(Martini martini) {
			String id = martini.getId();
			serialized.addProperty("id", id);
		}

		protected void setLine(Martini martini) {
			int line = martini.getScenarioLine();
			serialized.addProperty("line", line);
		}

		protected void setName(Martini martini) {
			String name = martini.getScenarioName();
			serialized.addProperty("name", name);
		}

		protected void setCategories(Martini martini) {
			Set<String> categorizations = categories.getCategorizations(martini);
			JsonElement serializedCategories = context.serialize(categorizations, Set.class);
			serialized.add("categories", serializedCategories);
		}

		protected void setTags(Martini martini) {
			Collection<MartiniTag> tags = martini.getTags();
			JsonElement serializedTags = context.serialize(tags, List.class);
			serialized.add("tags", serializedTags);
		}

		protected void setStatus() {
			Status status = result.getStatus();
			serialized.addProperty("status", status.name());
		}

		protected void setSteps() {
			List<StepResult> stepResults = result.getStepResults();
			Type type = TypeToken.getArray(StepResult.class).getType();
			StepResult[] stepResultArray = stepResults.toArray(new StepResult[stepResults.size()]);
			JsonElement serializedSteps = context.serialize(stepResultArray, type);
			serialized.add("steps", serializedSteps);
		}
	}
}
