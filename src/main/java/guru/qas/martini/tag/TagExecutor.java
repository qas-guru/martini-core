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

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;

import guru.qas.martini.Martini;

@SuppressWarnings("WeakerAccess")
class TagExecutor implements MethodExecutor {

	protected final String tagName;

	public TagExecutor(String tagName) {
		this.tagName = tagName;
	}

	@Override
	public TypedValue execute(
		EvaluationContext context,
		Object target,
		Object... arguments
	) throws AccessException {

		Martini martini = Martini.class.cast(target);
		String argument = null == arguments || arguments.length < 1 ? null : String.class.cast(arguments[0]);
		return execute(martini, argument);
	}

	protected TypedValue execute(Martini martini, String argument) {
		Collection<MartiniTag> tags = martini.getTags();
		boolean evaluation = false;
		for (Iterator<MartiniTag> i = tags.iterator(); !evaluation && i.hasNext(); ) {
			MartiniTag tag = i.next();
			String candidate = tag.getName();
			evaluation = null == argument ?
				tagName.equals(candidate) :
				tagName.equals(candidate) && argument.equals(tag.getArgument());
		}
		return new TypedValue(evaluation);
	}
}
