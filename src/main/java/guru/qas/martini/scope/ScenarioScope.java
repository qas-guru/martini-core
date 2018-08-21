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

package guru.qas.martini.scope;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	protected static final InheritableThreadLocal<LinkedHashMap<String, Object>> SCOPE_REF =
		new InheritableThreadLocal<LinkedHashMap<String, Object>>() {
			@Override
			protected LinkedHashMap<String, Object> initialValue() {
				return new LinkedHashMap<>();
			}
		};

	protected static final InheritableThreadLocal<LinkedHashMap<String, Runnable>> DESTRUCTION_CALLBACKS =
		new InheritableThreadLocal<LinkedHashMap<String, Runnable>>() {
			@Override
			protected LinkedHashMap<String, Runnable> initialValue() {
				return new LinkedHashMap<>();
			}
		};

	protected static final InheritableThreadLocal<MartiniResult> CONVO_REF = new InheritableThreadLocal<>();

	protected final Logger logger;

	protected ScenarioScope() {
		logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	@Nonnull
	public Object get(@Nonnull String name, @Nonnull ObjectFactory<?> objectFactory) {
		Map<String, Object> scope = SCOPE_REF.get();

		// Do NOT use computeIfAbsent(); will create a ConcurrentModificationException under
		// complex CGLib configurations.

		Object o;
		if (scope.containsKey(name)) {
			o = scope.get(name);
		}
		else {
			o = objectFactory.getObject();
			scope.put(name, o);
		}
		return o;
	}

	@Override
	public Object remove(@Nonnull String name) {
		runDestructionCallback(name);
		DESTRUCTION_CALLBACKS.get().remove(name);
		disposeBean(name);
		return SCOPE_REF.get().remove(name);
	}

	@Override
	public void registerDestructionCallback(@Nonnull String name, @Nonnull Runnable callback) {
		String trimmed = checkNotNull(name, "null String").trim();
		checkArgument(!trimmed.isEmpty(), "empty String");
		checkNotNull(callback, "null Runnable");
		HashMap<String, Runnable> callbacks = DESTRUCTION_CALLBACKS.get();
		callbacks.put(name, callback);
	}

	@Override
	public Object resolveContextualObject(@Nonnull String key) {
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
		String threadGroup = result.getThreadGroupName();
		String thread = result.getThreadName();
		String recipeId = result.getMartini().getRecipe().getId();
		UUID resultId = result.getId();
		return String.format("%s.%s.%s.%s.%s.%s", host, suite, threadGroup, thread, recipeId, resultId);
	}

	public void clear() {
		runDestructionCallbacks();
		disposeBeans();
		closeConversation();
	}

	protected void runDestructionCallbacks() {
		LinkedHashMap<String, Runnable> index = DESTRUCTION_CALLBACKS.get();
		Lists.reverse(Lists.newArrayList(index.keySet())).forEach(this::runDestructionCallback);
		DESTRUCTION_CALLBACKS.remove();
	}

	private void runDestructionCallback(String name) {
		Runnable callback = DESTRUCTION_CALLBACKS.get().get(name);
		runDestructionCallback(name, callback);
	}

	private void runDestructionCallback(String name, @Nullable Runnable callback) {
		if (null != callback) {
			try {
				callback.run();
			}
			catch (Exception e) {
				logger.warn("unable to complete destruction callback for bean {}", name, e);
			}
		}
	}

	protected void disposeBeans() {
		LinkedHashMap<String, Object> index = SCOPE_REF.get();
		Lists.reverse(Lists.newArrayList(index.keySet())).forEach(this::disposeBean);
		SCOPE_REF.remove();
	}

	protected void disposeBean(String name) {
		Object o = SCOPE_REF.get().get(name);
		dispose(name, o);
	}

	protected void dispose(String name, @Nullable Object bean) {
		if (DisposableBean.class.isInstance(bean)) {
			try {
				DisposableBean.class.cast(bean).destroy();
			}
			catch (Exception e) {
				logger.warn("unable to dispose of bean {}", name, bean);
			}
		}
	}

	protected void closeConversation() {
		CONVO_REF.remove();
	}
}
