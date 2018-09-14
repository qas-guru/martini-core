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

package guru.qas.martini.filter.category;


import javax.annotation.Nonnull;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;

import guru.qas.martini.Martini;
import guru.qas.martini.tag.Categories;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class CategoryExecutor implements MethodExecutor {

	protected final Categories categories;

	protected CategoryExecutor(Categories categories) {
		this.categories = checkNotNull(categories, "null Categories");
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
			checkArgument(1 == arguments.length, "expected a single argument");
			checkArgument(String.class.isInstance(arguments[0]),
				"expected a single String argument, got %s", null == arguments[0] ? null : arguments[0].getClass());

			Martini martini = Martini.class.cast(target);
			String category = String.class.cast(arguments[0]).trim();
			return execute(martini, category);
		}
		catch (Exception e) {
			throw new AccessException("filter exception", e);
		}
	}

	public TypedValue execute(Martini martini, String category) {
		boolean evaluation = martini.getTags().stream()
			.anyMatch(tag -> categories.isMatch(category, tag));
		return new TypedValue(evaluation);
	}
}
