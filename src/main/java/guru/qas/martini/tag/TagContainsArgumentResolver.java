/*
Copyright 2017 Penny Rohr Curich

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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.TypedValue;

import static com.google.common.base.Preconditions.*;

public class TagContainsArgumentResolver implements MethodResolver, MethodExecutor {

	@Override
	public MethodExecutor resolve(
		EvaluationContext context,
		Object targetObject,
		String name,
		List<TypeDescriptor> argumentTypes
	) throws AccessException {

		return name.equals("containsArgument") ? this : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypedValue execute(
		EvaluationContext context,
		Object target,
		Object... arguments
	) throws AccessException {
		checkNotNull(arguments, "null Object[]");
		checkState(1 == arguments.length, "wrong number of arguments, expected single String but got %s", arguments);
		Object o = arguments[0];
		checkState(String.class.isInstance(o), "argument is not an instance of String");
		String argument = String.class.cast(o);

		boolean evaluation = false;
		if (null != target) {
			checkState(Collection.class.isInstance(target), "operating on non-Collection target %s", target);
			Collection collection = Collection.class.cast(target);
			for (Iterator<Object> i = collection.iterator(); !evaluation && i.hasNext(); ) {
				o = i.next();
				checkState(MartiniTag.class.isInstance(o), "operating on non-MartiniTag target %s", o);
				MartiniTag tag = MartiniTag.class.cast(o);
				String tagArgument = tag.getArgument();
				evaluation = argument.equals(tagArgument);
			}
		}
		return new TypedValue(evaluation);
	}
}
