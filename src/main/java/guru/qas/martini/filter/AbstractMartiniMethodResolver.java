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

import javax.annotation.Nonnull;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;

import com.google.common.base.Joiner;

import guru.qas.martini.Martini;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractMartiniMethodResolver implements MethodResolver {

	protected final AtomicReference<MethodExecutor> ref;

	protected AbstractMartiniMethodResolver() {
		ref = new AtomicReference<>();
	}

	@Override
	public MethodExecutor resolve(
		@Nonnull EvaluationContext context,
		@Nonnull Object o,
		@Nonnull String name,
		@Nonnull List<TypeDescriptor> argumentTypes
	) throws AccessException {
		try {
			checkNotNull(context, "null EvaluationContext");
			checkNotNull(o, "null Object");
			checkNotNull(name, "null String");
			checkNotNull(argumentTypes, "null List");

			return Martini.class.isInstance(o) ? resolve(name, argumentTypes) : null;
		}
		catch (Exception e) {
			String joined = Joiner.on(", ").join(argumentTypes);
			String message = String.format("unable to resolve filter %s with arguments %s", name, joined);
			throw new AccessException(message, e);
		}
	}

	protected MethodExecutor resolve(String name, List<TypeDescriptor> argumentTypes) {
		return isNameMatch(name) && isArgumentCountMatch(argumentTypes) && isArgumentTypesMatch(argumentTypes)
			? getMethodExecutor(name) : null;
	}

	protected abstract boolean isNameMatch(String name);

	protected boolean isArgumentCountMatch(List<TypeDescriptor> argumentTypes) {
		return 1 == argumentTypes.size();
	}

	protected boolean isArgumentTypesMatch(List<TypeDescriptor> argumentTypes) {
		TypeDescriptor argumentTypeDescriptor = argumentTypes.get(0);
		Class<?> type = argumentTypeDescriptor.getObjectType();
		return String.class.isAssignableFrom(type);
	}

	protected abstract MethodExecutor getMethodExecutor(String name);
}
