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

import gherkin.ast.Feature;
import guru.qas.martini.event.AfterScenarioEvent;
import guru.qas.martini.event.AfterSuiteEvent;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.result.MartiniResult;

@SuppressWarnings({"WeakerAccess", "unused"})
@Component
@Lazy
public class JsonSuiteMarshaller implements InitializingBean, DisposableBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonSuiteMarshaller.class);

	protected final WritableResource resource;
	protected final Monitor monitor;

	protected MartiniResultSerializer martiniResultSerializer;
	protected SuiteIdentifierSerializer suiteIdentifierSerializer;
	protected FeatureSerializer featureSerializer;
	protected HostSerializer hostSerializer;
	protected OutputStream outputStream;
	protected JsonWriter jsonWriter;
	protected Gson gson;

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

	public JsonSuiteMarshaller(WritableResource resource) {
		this.resource = resource;
		this.monitor = new Monitor();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
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
			.generateNonExecutableJson()
			.serializeNulls();
	}

	protected void registerTypeAdapters(GsonBuilder builder) {
		builder.registerTypeAdapter(MartiniResult.class, martiniResultSerializer);
		builder.registerTypeAdapter(SuiteIdentifier.class, suiteIdentifierSerializer);
		builder.registerTypeAdapter(NetworkInterface.class, hostSerializer);
		builder.registerTypeAdapter(Feature.class, featureSerializer);
	}

	@EventListener
	public void handleAfterScenarioEvent(AfterScenarioEvent event) {
		MartiniResult result = event.getPayload();
		monitor.enter();
		try {
			gson.toJson(result, MartiniResult.class, jsonWriter);
			jsonWriter.flush();
		}
		catch (Exception e) {
			LOGGER.warn("unable to serialize MartiniResult {}", result, e);
		}
		finally {
			monitor.leave();
		}
	}

	@EventListener
	public void handleAfterSuiteEvent(AfterSuiteEvent ignored) {
		try {
			jsonWriter.flush();
			jsonWriter.close();
			closeOutputStream();
		}
		catch (IOException e) {
			LOGGER.error("unable to close json", e);
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
