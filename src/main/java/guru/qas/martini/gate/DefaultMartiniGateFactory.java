/*
Copyright 2018 Penny Rohr Curich

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

package guru.qas.martini.gate;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.env.Environment;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import guru.qas.martini.annotation.Gated;
import guru.qas.martini.step.StepImplementation;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultMartiniGateFactory implements MartiniGateFactory, InitializingBean {

	protected enum Source {
		METHOD, CLASS
	}

	protected final Environment environment;
	protected final Cache<String, Optional<Semaphore>> index;
	protected final Logger logger;

	protected boolean ignoringGates;
	protected int defaultGatePermits;

	@Autowired
	DefaultMartiniGateFactory(Environment environment) {
		this.environment = environment;
		index = CacheBuilder.newBuilder().build();
		this.logger = LoggerFactory.getLogger(getClass());
	}

	@Override
	public void afterPropertiesSet() {
		ignoringGates = environment.getProperty(MartiniGateFactory.PROPERTY_IGNORING_GATES, boolean.class, false);
		defaultGatePermits = environment.getProperty(MartiniGateFactory.PROPERTY_DEFAULT_GATE_PERMITS, int.class, 1);
	}

	@Nonnull
	@Override
	public Collection<MartiniGate> getGates(@Nullable StepImplementation implementation) {
		Method method = ignoringGates || null == implementation ? null : implementation.getMethod().orElse(null);
		return null == method ? Collections.emptyList() : getGates(method);
	}

	protected Collection<MartiniGate> getGates(Method method) {
		Class<?> declaringClass = method.getDeclaringClass();
		Gated[] classAnnotations = declaringClass.getDeclaredAnnotationsByType(Gated.class);
		Gated[] methodAnnotations = method.getDeclaredAnnotationsByType(Gated.class);

		ArrayListMultimap<String, Source> map = ArrayListMultimap.create();
		add(classAnnotations, Source.CLASS, map);
		add(methodAnnotations, Source.METHOD, map);

		return getGates(map);
	}

	protected void add(Gated[] annotations, Source column, Multimap<String, Source> gates) {
		Arrays.stream(annotations).forEach(a -> {
			String row = a.name();
			gates.put(row, column);
		});
	}

	protected Collection<MartiniGate> getGates(Multimap<String, Source> map) {
		Set<String> gateNames = map.keySet();

		return gateNames.stream()
			.map(n -> {
				Semaphore semaphore = getSemaphore(n).orElse(null);
				return null == semaphore ? null : new DefaultMartiniGate(n, semaphore);
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	protected Optional<Semaphore> getSemaphore(String gateName) {
		try {
			return index.get(gateName, () -> {
				AtomicReference<Semaphore> ref = new AtomicReference<>();
				getPermits(gateName).ifPresent(permits -> {
					logger.info("creating {} gate semaphore with {} permits", gateName, permits);
					ref.set(new Semaphore(permits, true));
				});
				return Optional.ofNullable(ref.get());
			});
		}
		catch (Exception e) {
			throw new RuntimeException("unable to obtain semaphore for gate " + gateName, e);
		}
	}

	protected Optional<Integer> getPermits(String gateName) {
		String property = String.format(PROPERTY_GATE_PERMIT_TEMPLATE, gateName);
		String configured = environment.getProperty(property, String.valueOf(defaultGatePermits)).trim();
		Integer permits = PROPERTY_GATE_IGNORED.equals(configured) ? null : Integer.parseInt(configured);
		checkState(null == permits || permits > 0,
			"invalid permit setting %s for gate %s, must be %s or greater than zero",
			permits, gateName, PROPERTY_GATE_IGNORED);
		return Optional.ofNullable(permits);
	}
}