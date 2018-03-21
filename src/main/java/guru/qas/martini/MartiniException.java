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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkState;

/**
 * {@inheritDoc}
 */
@SuppressWarnings("WeakerAccess")
public class MartiniException extends RuntimeException {

	/**
	 * {@inheritDoc}
	 * <p>
	 * deprecated, use {@link guru.qas.martini.MartiniException.Builder new guru.qas.martini.MartiniException.Builder()}
	 */
	@Deprecated
	public MartiniException() {
		super();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * deprecated, use {@link guru.qas.martini.MartiniException.Builder new guru.qas.martini.MartiniException.Builder()}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public MartiniException(String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * deprecated, use {@link guru.qas.martini.MartiniException.Builder new guru.qas.martini.MartiniException.Builder()}
	 */
	@Deprecated
	public MartiniException(String message, Throwable cause) {
		super(message, cause);
	}

	@SuppressWarnings("unused")
	public static class Builder {

		protected ResourceBundle messageBundle;
		protected Locale locale;
		protected String key;
		protected Object[] arguments;
		protected Exception cause;

		public Builder() {
		}

		public Builder setResourceBundle(ResourceBundle b) {
			this.messageBundle = b;
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

		@SuppressWarnings("deprecation")
		public MartiniException build() {
			checkState(null != messageBundle, "ResourceBundle not set");
			checkState(null != key, "String key not set");
			String message = getMessage();
			return null == cause ? new MartiniException(message) : new MartiniException(message, cause);
		}

		protected String getMessage() {
			String template = messageBundle.getString(key);
			Locale locale = messageBundle.getLocale();
			MessageFormat formatter = new MessageFormat(template, locale);
			return formatter.format(arguments);
		}
	}
}