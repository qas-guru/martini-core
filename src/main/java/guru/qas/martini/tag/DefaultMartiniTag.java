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

package guru.qas.martini.tag;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gherkin.pickles.PickleTag;
import guru.qas.martini.MartiniException;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class DefaultMartiniTag implements MartiniTag {

	protected final String name;
	protected final String argument;

	public String getName() {
		return name;
	}

	public String getArgument() {
		return argument;
	}

	protected DefaultMartiniTag(String name, String argument) {
		this.name = name;
		this.argument = argument;
	}

	public static Builder builder() {
		return new Builder();
	}

	@SuppressWarnings("WeakerAccess")
	public static class Builder {

		protected static final Pattern PATTERN_SIMPLE = Pattern.compile("^@(.+)$");
		protected static final Pattern PATTERN_ARGUMENTED = Pattern.compile("^@(.+)\\(\"(.+)\"\\)$");

		private String name;
		private String argument;

		protected Builder() {
		}

		public Builder setName(String s) {
			this.name = null == s ? null : s.trim();
			return this;
		}

		public Builder setArgument(String s) {
			this.argument = null == s ? null : s.trim();
			return this;
		}

		public Builder setPickleTag(PickleTag tag) {
			String value = tag.getName().trim();
			Matcher matcher = PATTERN_ARGUMENTED.matcher(value);

			try {
				if (matcher.find()) {
					setName(matcher.group(1));
					setArgument(matcher.group(2));
				}
				else {
					matcher = PATTERN_SIMPLE.matcher(value);
					checkState(matcher.find(), "illegal tag syntax: %s", value);
					setName(matcher.group(1));
					setArgument(null);
				}
				checkState(!matcher.find(), "illegal tag syntax: %s", value);
			}
			catch (Exception e) {
				throw new MartiniException("unable to create DefaultMartiniTag", e);
			}
			return this;
		}

		public DefaultMartiniTag build() {
			checkState(null != name && !name.isEmpty(), "null or empty name");
			return new DefaultMartiniTag(name, argument);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DefaultMartiniTag)) {
			return false;
		}
		DefaultMartiniTag that = (DefaultMartiniTag) o;
		return Objects.equals(getName(), that.getName()) &&
			Objects.equals(getArgument(), that.getArgument());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getArgument());
	}
}
