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

package guru.qas.martini.filter.gated;

import java.util.List;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.MethodExecutor;

import guru.qas.martini.filter.AbstractMartiniCachingMethodResolver;

@SuppressWarnings("WeakerAccess")
public class GatedResolver extends AbstractMartiniCachingMethodResolver {

	public static final String NAME = "isGated";

	public GatedResolver() {
		super();
	}

	protected boolean isNameMatch(String name) {
		return NAME.equals(name);
	}

	protected boolean isArgumentCountMatch(List<TypeDescriptor> argumentTypes) {
		return argumentTypes.isEmpty() || 1 == argumentTypes.size();
	}

	protected boolean isArgumentTypesMatch(List<TypeDescriptor> argumentTypes) {
		boolean evaluation = argumentTypes.isEmpty();
		if (!evaluation) {
			TypeDescriptor argumentTypeDescriptor = argumentTypes.get(0);
			Class<?> type = argumentTypeDescriptor.getObjectType();
			evaluation = String.class.isAssignableFrom(type);
		}
		return evaluation;
	}

	@Override
	protected MethodExecutor getMethodExecutor(String name) {
		return new GatedExecutor();
	}
}
