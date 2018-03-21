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

package guru.qas.martini.i18n;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

@SuppressWarnings("WeakerAccess")
public class MessageSources {

	protected static final ConcurrentHashMap<Class, ResourceBundleMessageSource> INDEX = new ConcurrentHashMap<>();

	public static MessageSource getMessageSource(Class c) {
		return INDEX.computeIfAbsent(c, r -> {
			ResourceBundleMessageSource source = new ResourceBundleMessageSource();
			source.setFallbackToSystemLocale(true);
			source.setBundleClassLoader(c.getClassLoader());
			source.setBasename(c.getName());
			source.setUseCodeAsDefaultMessage(true);
			source.setCacheSeconds(-1);
			return source;
		});
	}
}
