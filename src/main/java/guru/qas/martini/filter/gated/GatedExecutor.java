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

package guru.qas.martini.filter.gated;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;

import com.google.common.collect.Lists;

import gherkin.ast.Step;
import guru.qas.martini.Martini;
import guru.qas.martini.annotation.Gated;
import guru.qas.martini.step.StepImplementation;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class GatedExecutor implements MethodExecutor {

	protected GatedExecutor() {
		super();
	}

	@Nonnull
	@Override
	public TypedValue execute(
		@Nonnull EvaluationContext context,
		@Nonnull Object target,
		@Nonnull Object... arguments
	) throws AccessException {

		try {
			checkNotNull(context, "null EvaluationContext");
			checkNotNull(target, "null Object");
			checkNotNull(arguments, "null Object[]");
			checkArgument(Martini.class.isInstance(target), "Object not an instance of Martini");
			checkArgument(arguments.length < 2, "expected zero or one String argument");

			Object argument = 1 == arguments.length ? arguments[0] : null;
			checkArgument(null == argument || String.class.isInstance(argument),
				"expected argument to be a String, found %s", null == argument ? null : argument.getClass());

			Martini martini = Martini.class.cast(target);
			String gateName = null == argument ? null : String.class.cast(argument);
			return execute(martini, gateName);
		}
		catch (Exception e) {
			throw new AccessException("filter exception", e);
		}
	}

	public TypedValue execute(Martini martini, @Nullable String gateName) {
		Map<Step, StepImplementation> stepIndex = martini.getStepIndex();
		Collection<StepImplementation> values = stepIndex.values();
		boolean evaluation = values.stream()
			.map(stepImplementation -> stepImplementation.getMethod().orElse(null))
			.filter(Objects::nonNull)
			.map(method -> method.getDeclaredAnnotationsByType(Gated.class))
			.flatMap(gateds -> Lists.newArrayList(gateds).stream())
			.anyMatch(gated -> null == gateName || gateName.equals(gated.name()));
		return new TypedValue(evaluation);
	}
}