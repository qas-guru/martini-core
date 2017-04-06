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

import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("WeakerAccess")
public class DefaultSuiteIdentifier implements SuiteIdentifier {

	private final UUID id;
	private final String name;
	private final String hostname;
	private final String hostAddress;

	@Override
	public UUID getId() {
		return id;
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
		return null;
	}

	protected DefaultSuiteIdentifier(UUID id, String name, String hostname, String hostAddress) {
		this.id = id;
		this.name = name;
		this.hostname = hostname;
		this.hostAddress = hostAddress;
	}

	public static Builder builder() {
		return new Builder();
	}

	@SuppressWarnings("WeakerAccess")
	public static class Builder {

		protected UUID id;
		protected String name;
		protected String hostname;
		protected String hostAddress;

		protected Builder() {
		}

		public Builder setId(UUID id) {
			this.id = id;
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

		public SuiteIdentifier build() {
			checkState(null != id, "null UUID");
			checkState(null != name && !name.isEmpty(), "null or empty suite name");
			return new DefaultSuiteIdentifier(id, name, hostname, hostAddress);
		}
	}
}
