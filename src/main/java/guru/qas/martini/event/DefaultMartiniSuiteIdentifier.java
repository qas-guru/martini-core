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
public class DefaultMartiniSuiteIdentifier implements MartiniSuiteIdentifier {

	private final long timestamp;
	private final String hostname;
	private final String suiteName;
	private final UUID id;

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String getHostname() {
		return hostname;
	}

	@Override
	public String getSuiteName() {
		return suiteName;
	}

	@Override
	public UUID getId() {
		return id;
	}

	protected DefaultMartiniSuiteIdentifier(long timestamp, String hostname, String suiteName, UUID id) {
		this.timestamp = timestamp;
		this.hostname = hostname;
		this.suiteName = suiteName;
		this.id = id;
	}

	public static Builder builder() {
		return new Builder();
	}

	@SuppressWarnings("WeakerAccess")
	public static class Builder {
		protected long timestamp;
		protected String hostname;
		protected String suiteName;
		protected UUID id;

		protected Builder() {
		}

		public Builder setTimetamp(long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder setHostname(String s) {
			this.hostname = normalize(s);
			return this;
		}

		protected String normalize(String s) {
			return null == s ? null : s.trim();
		}

		public Builder setSuiteName(String s) {
			this.suiteName = normalize(s);
			return this;
		}

		public Builder setId(UUID id) {
			this.id = id;
			return this;
		}

		public MartiniSuiteIdentifier build() {
			checkState(null != hostname && !hostname.isEmpty(), "null or empty hostname");
			checkState(null != suiteName && !suiteName.isEmpty(), "null or empty suite name");
			checkState(null != id, "null UUID");
			return new DefaultMartiniSuiteIdentifier(timestamp, hostname, suiteName, id);
		}
	}
}
