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

package guru.qas.martini.scope;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.result.MartiniResult;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
@Component
public class ScenarioScope implements Scope {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioScope.class);

	private static final InheritableThreadLocal<Map<String, Object>> SCOPE_REF = new InheritableThreadLocal<Map<String, Object>>() {
		@Override
		protected Map<String, Object> initialValue() {
			return new HashMap<>();
		}
	};

	private static final InheritableThreadLocal<LinkedHashMap<String, Runnable>> DESTRUCTION_CALLBACKS =
		new InheritableThreadLocal<LinkedHashMap<String, Runnable>>() {
			@Override
			protected LinkedHashMap<String, Runnable> initialValue() {
				return new LinkedHashMap<>();
			}
		};

	private static final InheritableThreadLocal<MartiniResult> CONVO_REF = new InheritableThreadLocal<>();

	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Map<String, Object> scope = SCOPE_REF.get();
		return scope.computeIfAbsent(name, k -> objectFactory.getObject());
	}

	@Override
	public Object remove(String name) {
		Map<String, Object> scope = SCOPE_REF.get();
		return scope.remove(name);
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		String trimmed = checkNotNull(name, "null String").trim();
		checkArgument(!trimmed.isEmpty(), "empty String");
		checkNotNull(callback, "null Runnable");
		HashMap<String, Runnable> callbacks = DESTRUCTION_CALLBACKS.get();
		callbacks.put(name, callback);
	}

	@Override
	public Object resolveContextualObject(String key) {
		return "martiniResult".equals(key) ? CONVO_REF.get() : null;
	}

	public void setScenarioIdentifier(MartiniResult result) {
		CONVO_REF.set(result);
	}

	@Override
	public String getConversationId() {
		MartiniResult result = CONVO_REF.get();
		return getConversationId(result);
	}

	protected String getConversationId(MartiniResult result) {
		SuiteIdentifier suiteIdentifier = result.getSuiteIdentifier();
		String host = suiteIdentifier.getHostname();
		String suite = suiteIdentifier.getName();
		String id = suiteIdentifier.getId();
		String threadGroup = result.getThreadGroupName();
		String thread = result.getThreadName();
		String recipeId = result.getMartini().getRecipe().getId();
		return String.format("%s.%s.%s.%s.%s.%s", host, suite, id, threadGroup, thread, recipeId);
	}

	public void clear() {
		runDestructionCallbacks();
		disposeBeans();
		CONVO_REF.remove();
	}

	private void runDestructionCallbacks() {
		LinkedHashMap<String, Runnable> index = DESTRUCTION_CALLBACKS.get();
		List<String> names = Lists.reverse(Lists.newArrayList(index.keySet()));
		for (String name : names) {
			Runnable callback = index.remove(name);
			run(name, callback);
		}
	}

	private static void run(String name, Runnable callback) {
		try {
			callback.run();
		}
		catch (Exception e) {
			LOGGER.warn("unable to complete destruction callback for bean {}", name, e);
		}
	}

	private void disposeBeans() {
		Map<String, Object> index = SCOPE_REF.get();
		for (Map.Entry<String, Object> mapEntry : index.entrySet()) {
			String name = mapEntry.getKey();
			Object bean = mapEntry.getValue();
			dispose(name, bean);
		}
	}

	private static void dispose(String name, Object bean) {
		if (DisposableBean.class.isInstance(bean)) {
			DisposableBean disposable = DisposableBean.class.cast(bean);
			try {
				disposable.destroy();
			}
			catch (Exception e) {
				LOGGER.warn("unable to dispose of bean {}", name, bean);
			}
		}
	}
}
