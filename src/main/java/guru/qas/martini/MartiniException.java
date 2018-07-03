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

package guru.qas.martini;

import java.util.Locale;

import org.springframework.context.MessageSource;

import static com.google.common.base.Preconditions.checkState;

/**
 * {@inheritDoc}
 */
@SuppressWarnings("WeakerAccess")
public class MartiniException extends RuntimeException {

	public MartiniException(String message) {
		super(message);
	}

	public MartiniException(String message, Throwable cause) {
		super(message, cause);
	}

	@SuppressWarnings({"unused", "WeakerAccess"})
	public static class Builder {

		protected MessageSource messageSource;
		protected Locale locale;
		protected String key;
		protected Object[] arguments;
		protected Exception cause;

		public Builder() {
			locale = Locale.getDefault();
		}

		public Builder setMessageSource(MessageSource s) {
			this.messageSource = s;
			return this;
		}

		public Builder setLocale(Locale l) {
			this.locale = null == l ? Locale.getDefault() : l;
			return this;
		}

		public Builder setKey(String s) {
			this.key = s;
			return this;
		}

		public Builder setArguments(Object... o) {
			this.arguments = o;
			return this;
		}

		public Builder setCause(Exception e) {
			this.cause = e;
			return this;
		}

		public MartiniException build() {
			String message = getMessage();
			return null == cause ? new MartiniException(message) : new MartiniException(message, cause);
		}

		protected String getMessage() {
			checkState(null != messageSource, "MessageSource not set");
			checkState(null != key, "String key not set");
			return messageSource.getMessage(key, arguments, key, locale);
		}
	}
}