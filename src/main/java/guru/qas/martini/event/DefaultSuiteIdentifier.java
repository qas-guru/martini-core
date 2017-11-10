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

package guru.qas.martini.event;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DefaultSuiteIdentifier implements SuiteIdentifier {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSuiteIdentifier.class);

	private final UUID id;
	private final Long startTimestamp;
	private final ImmutableSet<String> profiles;
	private final ImmutableMap<String, String> environmentVariables;
	private final String name;
	private final String hostname;
	private final String hostAddress;
	private final String username;

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public Long getStartTimestamp() {
		return startTimestamp;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getHostname() {
		return hostname;
	}

	@Override
	public String getHostAddress() {
		return hostAddress;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public ImmutableSet<String> getProfiles() {
		return profiles;
	}

	@Override
	public ImmutableMap<String, String> getEnvironmentVariables() {
		return environmentVariables;
	}

	protected DefaultSuiteIdentifier(
		UUID id,
		Long startTimestamp,
		String name,
		String hostname,
		String hostAddress,
		String username,
		ImmutableSet<String> profiles,
		ImmutableMap<String, String> environmentVariables
	) {
		this.id = id;
		this.startTimestamp = startTimestamp;
		this.name = name;
		this.hostname = hostname;
		this.hostAddress = hostAddress;
		this.username = username;
		this.profiles = profiles;
		this.environmentVariables = environmentVariables;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		protected static final String UNKNOWN = "unknown";
		protected String hostname = UNKNOWN;
		protected String address = UNKNOWN;
		protected Map<String, String> environmentVariables = System.getenv();
		protected String name;
		protected long startupTimestamp;

		protected Builder() {
		}

		public DefaultSuiteIdentifier build(ApplicationContext context) {
			setHostInformation();
			Environment environment = context.getEnvironment();
			return new DefaultSuiteIdentifier(UUID.randomUUID(),
				context.getStartupDate(),
				context.getDisplayName(),
				hostname,
				address,
				System.getProperty("user.name"),
				ImmutableSet.copyOf(environment.getActiveProfiles()),
				ImmutableMap.copyOf(System.getenv()));
		}

		protected void setHostInformation() {
			try {
				InetAddress localHost = InetAddress.getLocalHost();
				hostname = localHost.getHostName();
				address = localHost.getHostAddress();
			}
			catch (UnknownHostException e) {
				LOGGER.warn("unable to ascertain hostname and address");
			}
		}

	}
}
