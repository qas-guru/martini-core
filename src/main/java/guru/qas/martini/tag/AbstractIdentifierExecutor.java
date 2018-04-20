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

package guru.qas.martini.tag;

import javax.annotation.Nonnull;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;

import guru.qas.martini.Martini;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractIdentifierExecutor implements MethodExecutor {

	@Nonnull
	@Override
	public TypedValue execute(
		@Nonnull EvaluationContext context,
		@Nonnull Object target,
		@Nonnull Object... arguments
	) {
		assertValidArguments(arguments);
		Object argument = arguments[0];
		checkArgument(null != argument, "expected at least one argument");

		Martini martini = Martini.class.cast(target);
		String identifier = argument.toString();
		return execute(martini, identifier);
	}

	protected abstract void assertValidArguments(Object... arguments) throws IllegalArgumentException;

	public TypedValue execute(Martini martini, String identifier) {
		String martiniIdentifier = getIdentifier(martini);
		String normalizedMartiniIdentifier = getNormalized(martiniIdentifier);
		String normalizedIdentifier = getNormalized(identifier);
		boolean evaluation = normalizedMartiniIdentifier.equals(normalizedIdentifier);
		return new TypedValue(evaluation);
	}

	protected abstract String getIdentifier(Martini martini);

	private String getNormalized(String s) {
		return s.replaceAll("\\s+", " ").trim();
	}
}
