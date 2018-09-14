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

package guru.qas.martini.filter.tag;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;

import guru.qas.martini.Martini;
import guru.qas.martini.tag.MartiniTag;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class TagExecutor implements MethodExecutor {

	protected final String name;

	protected TagExecutor(String name) {
		this.name = checkNotNull(name).substring(2);
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
			checkArgument(Martini.class.isInstance(target), "Object not an instance of Martini: %s", target.getClass());

			Martini martini = Martini.class.cast(target);
			String argument = arguments.length > 0 ? String.class.cast(arguments[0]) : null;
			return execute(martini, argument);
		}
		catch (Exception e) {
			throw new AccessException("unable to execute filter", e);
		}
	}

	protected TypedValue execute(Martini martini, String argument) {
		Collection<MartiniTag> tags = martini.getTags();

		boolean evaluation = tags.stream()
			.filter(this::isNameMatch)
			.anyMatch(tag -> isArgumentMatch(argument, tag));
		return new TypedValue(evaluation);
	}

	protected boolean isNameMatch(MartiniTag tag) {
		return name.equals(tag.getName());
	}

	protected boolean isArgumentMatch(@Nullable String argument, MartiniTag tag) {
		return null == argument || argument.equals(tag.getArgument());
	}
}
