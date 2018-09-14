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

package guru.qas.martini.filter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.MethodExecutor;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractMartiniCachingMethodResolver extends AbstractMartiniMethodResolver {

	protected final AtomicReference<MethodExecutor> ref;

	protected AbstractMartiniCachingMethodResolver() {
		super();
		ref = new AtomicReference<>();
	}

	protected MethodExecutor resolve(String name, List<TypeDescriptor> argumentTypes) {
		MethodExecutor executor = null;
		if (isNameMatch(name) && isArgumentCountMatch(argumentTypes) && isArgumentTypesMatch(argumentTypes)) {
			if (null == ref.get()) {
				ref.compareAndSet(null, getMethodExecutor(name));
			}
			executor = ref.get();
		}
		return executor;
	}
}
