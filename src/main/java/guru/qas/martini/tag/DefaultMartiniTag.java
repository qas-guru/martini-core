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

package guru.qas.martini.tag;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.MessageSource;

import gherkin.pickles.PickleTag;
import guru.qas.martini.MartiniException;
import guru.qas.martini.i18n.MessageSources;

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

	@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
	public static class Builder {

		protected static final String KEY_ILLEGAL_SYNTAX = "martini.illegal.tag.syntax";

		protected static final Pattern PATTERN_SIMPLE = Pattern.compile("^@(.+)$");
		protected static final Pattern PATTERN_ARGUMENTED = Pattern.compile("^@(.+)\\(\"(.+)\"\\)$");

		protected PickleTag pickleTag;

		protected Builder() {
		}

		public Builder setPickleTag(PickleTag t) {
			this.pickleTag = t;
			return this;
		}

		public DefaultMartiniTag build() {
			checkState(null != pickleTag, "PickleTag not set");

			DefaultMartiniTag tag = getArgumented().orElseGet(this::getSimple);
			if (null == tag) {
				MessageSource messageSource = MessageSources.getMessageSource(DefaultMartiniTag.class);
				String input = getInput();
				throw new MartiniException.Builder()
					.setMessageSource(messageSource)
					.setKey(KEY_ILLEGAL_SYNTAX)
					.setArguments(input)
					.build();
			}
			return tag;
		}

		protected String getInput() {
			String name = pickleTag.getName();
			return name.trim();
		}

		protected Optional<DefaultMartiniTag> getArgumented() {
			String input = getInput();
			Matcher matcher = PATTERN_ARGUMENTED.matcher(input);
			DefaultMartiniTag tag = null;
			if (matcher.find()) {
				String name = matcher.group(1);
				String argument = matcher.group(2);
				tag = new DefaultMartiniTag(name, argument);
			}
			return Optional.ofNullable(tag);
		}

		protected DefaultMartiniTag getSimple() {
			String input = getInput();
			Matcher matcher = PATTERN_SIMPLE.matcher(input);
			DefaultMartiniTag tag = null;
			if (matcher.find()) {
				String name = matcher.group(1);
				tag = new DefaultMartiniTag(name, null);
			}
			return tag;

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
