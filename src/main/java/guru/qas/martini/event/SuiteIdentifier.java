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

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class SuiteIdentifier implements Serializable {

	protected final String hostname;
	protected final String suiteName;
	protected final String threadGroupName;

	public String getHostname() {
		return hostname;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public String getThreadGroup() {
		return threadGroupName;
	}

	protected SuiteIdentifier(String hostname, String suiteName, String threadGroupName) {
		this.hostname = hostname;
		this.suiteName = suiteName;
		this.threadGroupName = threadGroupName;
	}

	public static Builder builder() {
		return new Builder();
	}

	@SuppressWarnings("WeakerAccess")
	public static class Builder {

		protected String hostName;
		protected String suiteName;
		protected String threadGroupName;

		protected Builder() {
		}

		public Builder setHostName(String s) {
			this.hostName = s;
			return this;
		}

		public Builder setSuiteName(String s) {
			this.suiteName = s;
			return this;
		}

		public Builder setThreadGroupName(String s) {
			this.threadGroupName = s;
			return this;
		}

		public SuiteIdentifier build() {
			String trimmedHost = checkNotNull(hostName, "host name not set").trim();
			String trimmedSuite = checkNotNull(suiteName, "suite name not set").trim();
			String trimmedThreadGroup = checkNotNull(threadGroupName, "thread group name not set").trim();
			return new SuiteIdentifier(trimmedHost, trimmedSuite, trimmedThreadGroup);
		}
	}
}
