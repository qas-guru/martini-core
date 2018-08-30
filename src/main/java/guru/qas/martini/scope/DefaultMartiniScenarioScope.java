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

import java.util.Optional;
import java.util.Stack;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Configurable;

import guru.qas.martini.Martini;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.result.MartiniResult;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultMartiniScenarioScope implements MartiniScenarioScope {

	protected static final InheritableThreadLocal<MartiniResult> CONVERSATION =
		new InheritableThreadLocal<>();

	protected static final InheritableThreadLocal<Stack<Scoped>> SCOPED =
		new InheritableThreadLocal<Stack<Scoped>>() {
			@Override
			protected Stack<Scoped> initialValue() {
				return new Stack<>();
			}
		};

	protected final Logger logger;

	public DefaultMartiniScenarioScope() {
		logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public void setScenarioIdentifier(@Nullable MartiniResult result) {
		CONVERSATION.set(result);
	}

	@Override
	public Optional<MartiniResult> getMartiniResult() {
		MartiniResult martiniResult = CONVERSATION.get();
		return Optional.ofNullable(martiniResult);
	}

	@Override
	@Nullable
	public Object resolveContextualObject(@Nonnull String key) {
		checkNotNull(key, "null String");
		return "martiniResult".equals(key) ? CONVERSATION.get() : null;
	}

	@Override
	@Nullable
	public String getConversationId() {
		MartiniResult result = CONVERSATION.get();
		return null == result ? null : getConversationId(result);
	}

	protected String getConversationId(MartiniResult result) {
		SuiteIdentifier suiteIdentifier = result.getSuiteIdentifier();
		String host = suiteIdentifier.getHostName().orElse(null);
		String suite = suiteIdentifier.getName();
		String threadGroup = result.getThreadGroupName();
		String thread = result.getThreadName();
		String recipeId = getRecipeId(result);
		UUID resultId = result.getId();
		return String.format("%s.%s.%s.%s.%s.%s", host, suite, threadGroup, thread, recipeId, resultId);
	}

	protected String getRecipeId(MartiniResult result) {
		Martini martini = result.getMartini();
		Recipe recipe = null == martini ? null : martini.getRecipe();
		return null == recipe ? null : recipe.getId();
	}

	@Override
	@Nonnull
	public Object get(@Nonnull String name, @Nonnull ObjectFactory<?> objectFactory) {
		checkNotNull(name, "null String");
		checkNotNull(objectFactory, "null ObjectFactory");

		Scoped scoped = getWrappedBean(name).orElseGet(() -> {
			Object bean = objectFactory.getObject();
			Scoped wrapped = Scoped.bean(name, bean);
			return SCOPED.get().push(wrapped);
		});
		return scoped.getObject();
	}

	protected Optional<Scoped> getWrappedBean(String name) {
		checkNotNull(name, "null String");
		return SCOPED.get().stream()
			.filter(Scoped::isBean)
			.filter(s -> s.getName().equals(name))
			.findFirst();
	}

	@Override
	public void registerDestructionCallback(@Nonnull String name, @Nonnull Runnable callback) {
		checkNotNull(name, "null String");
		checkNotNull(callback, "null Runnable");

		getDestructionCallback(name).ifPresent(s -> remove(name));
		Scoped wrapped = Scoped.destructionCallback(name, callback);
		SCOPED.get().push(wrapped);
	}

	protected Optional<Scoped> getDestructionCallback(String name) {
		checkNotNull(name, "null String");
		return SCOPED.get().stream()
			.filter(Scoped::isDestructionCallback)
			.filter(s -> s.getName().equals(name))
			.findFirst();
	}

	@Override
	public Object remove(@Nonnull String name) {
		checkNotNull(name, "null String");
		Object bean = dispose(name);
		destroy(name);
		return bean;
	}

	@Nullable
	protected Object dispose(String name) {
		Scoped wrapped = getWrappedBean(name).orElse(null);
		if (null != wrapped) {
			SCOPED.get().remove(wrapped);
			dispose(wrapped);
		}
		return null == wrapped ? null : wrapped.getObject();
	}

	protected void dispose(Scoped wrapped) {
		Object o = wrapped.getObject();
		if (DisposableBean.class.isInstance(o)) {
			DisposableBean disposableBean = DisposableBean.class.cast(o);
			try {
				disposableBean.destroy();
			}
			catch (Exception e) {
				String name = wrapped.getName();
				logger.warn("unable to destroy bean {}", name, e);
			}
		}
	}

	protected void destroy(@Nonnull String name) {
		checkNotNull(name, "null String");
		Scoped wrapped = getDestructionCallback(name).orElse(null);
		if (null != wrapped) {
			SCOPED.get().remove(wrapped);
			destroy(wrapped);
		}
	}

	protected void destroy(Scoped wrapped) {
		String name = wrapped.getName();
		Object o = wrapped.getObject();

		try {
			checkState(Runnable.class.isInstance(o), "callback is not of expected type Runnable");
			Runnable.class.cast(o).run();
		}
		catch (Exception e) {
			logger.warn("unable to execute destruction callback {}", name, e);
		}
	}

	@Override
	public void clear() {
		Stack<Scoped> scope = SCOPED.get();
		while (!scope.isEmpty()) {
			Scoped scoped = scope.pop();
			if (scoped.isBean()) {
				dispose(scoped);
			}
			else if (scoped.isDestructionCallback()) {
				destroy(scoped);
			}
		}
		closeConversation();
	}

	protected void closeConversation() {
		CONVERSATION.remove();
		SCOPED.remove();
	}
}
