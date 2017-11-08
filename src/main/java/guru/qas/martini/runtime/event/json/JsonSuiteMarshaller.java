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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.NetworkInterface;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.Monitor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import guru.qas.martini.Martini;
import guru.qas.martini.event.AfterScenarioEvent;
import guru.qas.martini.event.AfterSuiteEvent;
import guru.qas.martini.event.BeforeSuiteEvent;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.gherkin.FeatureWrapper;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.result.MartiniResult;
import guru.qas.martini.result.StepResult;
import guru.qas.martini.step.StepImplementation;

@SuppressWarnings({"WeakerAccess", "unused"})
@Component
@Lazy
public class JsonSuiteMarshaller implements InitializingBean, DisposableBean {

	protected static final Logger LOGGER = LoggerFactory.getLogger(JsonSuiteMarshaller.class);
	protected static final byte[] NEWLINE = "\n".getBytes();

	protected final WritableResource resource;
	protected final Monitor monitor;

	protected MartiniResultSerializer martiniResultSerializer;
	protected SuiteIdentifierSerializer suiteIdentifierSerializer;
	protected FeatureSerializer featureSerializer;
	protected StepResultSerializer stepResultSerializer;
	protected StepImplementationSerializer stepImplementationSerializer;
	protected HostSerializer hostSerializer;
	protected OutputStream outputStream;
	protected JsonWriter jsonWriter;
	protected Gson gson;

	protected HashSet<FeatureWrapper> serializedFeatures;

	@Autowired
	protected void setMartiniResultSerializer(MartiniResultSerializer s) {
		this.martiniResultSerializer = s;
	}

	@Autowired
	protected void setSuiteIdentifierSerializer(SuiteIdentifierSerializer s) {
		this.suiteIdentifierSerializer = s;
	}

	@Autowired
	protected void setHostSerializer(HostSerializer s) {
		this.hostSerializer = s;
	}

	@Autowired
	protected void setFeatureSerializer(FeatureSerializer s) {
		this.featureSerializer = s;
	}

	@Autowired
	protected void setStepResultSerializer(StepResultSerializer s) {
		this.stepResultSerializer = s;
	}

	@Autowired
	protected void setStepImplementationSerializer(StepImplementationSerializer s) {
		this.stepImplementationSerializer = s;
	}

	public JsonSuiteMarshaller(WritableResource resource) {
		this.resource = resource;
		this.monitor = new Monitor();
		serializedFeatures = new HashSet<>();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		serializedFeatures.clear();

		GsonBuilder builder = getGsonBuilder();
		registerTypeAdapters(builder);
		gson = builder.create();

		outputStream = resource.getOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		jsonWriter = gson.newJsonWriter(writer);
	}

	protected GsonBuilder getGsonBuilder() {
		return new GsonBuilder()
			.setPrettyPrinting()
			.setLenient()
			.serializeNulls();
	}

	protected void registerTypeAdapters(GsonBuilder builder) {
		builder.registerTypeAdapter(MartiniResult.class, martiniResultSerializer);
		builder.registerTypeAdapter(SuiteIdentifier.class, suiteIdentifierSerializer);
		builder.registerTypeAdapter(NetworkInterface.class, hostSerializer);
		builder.registerTypeAdapter(FeatureWrapper.class, featureSerializer);
		builder.registerTypeAdapter(StepResult.class, stepResultSerializer);
		builder.registerTypeAdapter(StepImplementation.class, stepImplementationSerializer);
	}

	@EventListener
	public void handleBeforeSuiteEvent(BeforeSuiteEvent event) {
		SuiteIdentifier identifier = event.getPayload();
		monitor.enter();
		try {
			serialize(identifier);
		}
		finally {
			monitor.leave();
		}
	}

	protected void serialize(SuiteIdentifier identifier) {
		try {
			gson.toJson(identifier, SuiteIdentifier.class, jsonWriter);
			jsonWriter.flush();
			outputStream.write(NEWLINE);
		}
		catch (Exception e) {
			LOGGER.warn("unable to serialize SuiteIdentifier {}", identifier, e);
		}
	}

	@EventListener
	public void handleAfterScenarioEvent(AfterScenarioEvent event) {
		MartiniResult result = event.getPayload();
		try {
			serializeFeature(result);
			serialize(result);
		}
		catch (Exception e) {
			LOGGER.warn("unable to serialize MartiniResult {}", result, e);
		}
	}

	protected void serializeFeature(MartiniResult result) throws IOException {
		Martini martini = result.getMartini();
		Recipe recipe = martini.getRecipe();
		FeatureWrapper feature = recipe.getFeatureWrapper();
		serialize(feature);
	}

	protected void serialize(FeatureWrapper feature) throws IOException {
		monitor.enter();
		try {
			if (!serializedFeatures.contains(feature)) {
				gson.toJson(feature, FeatureWrapper.class, jsonWriter);
				jsonWriter.flush();
				serializedFeatures.add(feature);
				outputStream.write(NEWLINE);
			}
		}
		finally {
			monitor.leave();
		}
	}

	protected void serialize(MartiniResult result) throws IOException {
		monitor.enter();
		try {
			gson.toJson(result, MartiniResult.class, jsonWriter);
			jsonWriter.flush();
			outputStream.write(NEWLINE);
		}
		finally {
			monitor.leave();
		}
	}

	@EventListener
	public void handleAfterSuiteEvent(AfterSuiteEvent ignored) {
		monitor.enter();
		try {
			jsonWriter.flush();
			jsonWriter.close();
			closeOutputStream();
		}
		catch (IOException e) {
			LOGGER.error("unable to close json", e);
		}
		finally {
			monitor.leave();
		}
	}

	protected void closeOutputStream() {
		if (null != outputStream) {
			try {
				outputStream.flush();
				outputStream.close();
			}
			catch (IOException e) {
				LOGGER.error("unable to close OutputStream for resource {}", resource);
			}
			finally {
				outputStream = null;
			}
		}
	}

	@Override
	public void destroy() throws Exception {
		closeOutputStream();
	}
}
