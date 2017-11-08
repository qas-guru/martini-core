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

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DefaultSuiteIdentifier implements SuiteIdentifier {

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

	@SuppressWarnings("WeakerAccess")
	public static class Builder {

		protected Long startupTimestamp;
		protected String name;
		protected String hostname;
		protected String hostAddress;
		protected String username;
		protected ImmutableSet<String> profiles;
		protected ImmutableMap<String, String> environmentVariables;

		protected Builder() {
		}

		public Builder setStartupTimestamp(Long timestamp) {
			this.startupTimestamp = timestamp;
			return this;
		}

		public Builder setName(String s) {
			this.name = normalize(s);
			return this;
		}

		protected String normalize(String s) {
			return null == s ? null : s.trim();
		}

		public Builder setHostname(String s) {
			this.hostname = normalize(s);
			return this;
		}

		public Builder setHostAddress(String s) {
			this.hostAddress = normalize(s);
			return this;
		}

		public Builder setUsername(String s) {
			this.username = s;
			return this;
		}

		public Builder setProfiles(Iterable<String> profiles) {
			this.profiles = null == profiles ? ImmutableSet.of() : ImmutableSet.copyOf(profiles);
			return this;
		}

		public Builder setEnvironmentVariables(Map<String, String> variables) {
			this.environmentVariables = null == variables ? ImmutableMap.of() : ImmutableMap.copyOf(variables);
			return this;
		}

		public SuiteIdentifier build() {
			checkState(null != name && !name.isEmpty(), "null or empty suite name");
			UUID id = UUID.randomUUID();
			return new DefaultSuiteIdentifier(
				id, startupTimestamp, name, hostname, hostAddress, username, profiles, environmentVariables);
		}
	}
}
