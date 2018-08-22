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

package guru.qas.martini.scope;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class Scoped {

	private enum Type {
		BEAN, DESTRUCTION_CALLBACK
	}

	private final Type type;
	private final String name;
	private final Object o;

	public boolean isBean() {
		return Type.BEAN.equals(type);
	}

	public boolean isDestructionCallback() {
		return Type.DESTRUCTION_CALLBACK.equals(type);
	}

	public String getName() {
		return name;
	}

	public Object getObject() {
		return o;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Scoped scoped = (Scoped) o;
		return type == scoped.type &&
			Objects.equal(name, scoped.name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type, name);
	}

	protected Scoped(Type type, String name, Object o) {
		this.type = checkNotNull(type, "null ScopedType");
		this.name = checkNotNull(name, "null String");
		this.o = checkNotNull(o, "null Object");
	}

	protected static Scoped bean(String name, Object bean) {
		checkNotNull(name, "null String");
		checkNotNull(bean, "null Object");
		return new Scoped(Type.BEAN, name, bean);
	}

	protected static Scoped destructionCallback(String name, Runnable callback) {
		checkNotNull(name, "null String");
		checkNotNull(callback, "null Runnable");
		return new Scoped(Type.DESTRUCTION_CALLBACK, name, callback);
	}
}