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

package guru.qas.martini.gherkin;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gherkin.pickles.PickleTag;
import guru.qas.martini.MartiniException;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class MartiniTag {

	protected final String name;
	protected final String argument;

	public String getName() {
		return name;
	}

	public String getArgument() {
		return argument;
	}

	protected MartiniTag(String name) {
		this(name, null);
	}

	protected MartiniTag(String name, String argument) {
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

		protected Builder() {
		}

		public MartiniTag build(PickleTag pickleTag) throws MartiniException {
			checkNotNull(pickleTag, "null PickleTag");
			String value = pickleTag.getName().trim();
			try {
				MartiniTag tag = getArgumented(value);
				return null == tag ? getSimple(value) : tag;
			}
			catch (Exception e) {
				throw new MartiniException("unable to create MartiniTag", e);
			}
		}

		protected MartiniTag getArgumented(String value) {
			Matcher matcher = PATTERN_ARGUMENTED.matcher(value);

			MartiniTag tag = null;
			if (matcher.find()) {
				String name = matcher.group(1);
				String argument = matcher.group(2).trim();
				tag = new MartiniTag(name, argument);
				checkState(!matcher.find(), "illegal tag syntax: %s", value);
			}
			return tag;
		}

		protected MartiniTag getSimple(String value) {
			Matcher matcher = PATTERN_SIMPLE.matcher(value);
			checkState(matcher.find(), "illegal tag syntax: %s", value);
			String name = matcher.group(1);
			return new MartiniTag(name);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MartiniTag)) {
			return false;
		}
		MartiniTag that = (MartiniTag) o;
		return Objects.equals(getName(), that.getName()) &&
			Objects.equals(getArgument(), that.getArgument());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getArgument());
	}
}
