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

import java.util.concurrent.Semaphore;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.env.Environment;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultMartiniGateFactory extends CacheLoader<String, MartiniGate> implements MartiniGateFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMartiniGateFactory.class);

	private final Environment environment;
	private final LoadingCache<String, MartiniGate> cache;

	DefaultMartiniGateFactory(Environment environment) {
		this.environment = environment;
		cache = CacheBuilder.newBuilder().build(this);
	}

	@Override
	public MartiniGate getGate(String name) {
		return cache.getUnchecked(name);
	}

	@Override
	public MartiniGate load(@Nonnull String name) {
		String trimmed = checkNotNull(name, "null String").trim();
		checkArgument(!trimmed.isEmpty(), "empty String");

		int permits = getPermits(trimmed);
		Semaphore semaphore = new Semaphore(permits, true);
		return new DefaultMartiniGate(trimmed, semaphore);
	}

	protected int getPermits(String name) {
		String property = String.format("martini.gate.permits.%s", name);
		String value = environment.getProperty(property, "1");

		Integer permits = null;
		try {
			permits = Integer.parseInt(value);
			checkArgument(permits > 0, "permits must be 1 or greater");
		}
		catch (Exception e) {
			LOGGER.warn("using default value 1 for {} instead of invalid configuration {}", property, value, e);
		}
		return null == permits ? 1 : permits;
	}
}