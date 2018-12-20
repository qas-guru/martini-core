/*
Copyright 2018 Penny Rohr Curich

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

import org.springframework.context.i18n.LocaleContextHolder;

import ch.qos.cal10n.IMessageConveyor;
import ch.qos.cal10n.MessageConveyor;

public abstract class Messages {

	protected Messages() {
	}

	public static String getMessage(Enum<?> key, Object... args) {
		IMessageConveyor messageConveyor = getMessageConveyor();
		return messageConveyor.getMessage(key, args);
	}

	/**
	 * @return MessageConveyor with currently configured Locale.
	 */
	public static IMessageConveyor getMessageConveyor() {
		Locale locale = LocaleContextHolder.getLocale();
		return new MessageConveyor(locale);
	}
}