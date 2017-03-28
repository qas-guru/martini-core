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
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class DefaultCategory implements Category {

	protected final String name;
	protected final ImmutableList<String> parentNames;

	public String getName() {
		return name;
	}

	public Iterable<String> getParentNames() {
		return parentNames;
	}

	public DefaultCategory(String name, @Nullable Collection<String> parentNames) {
		this.name = checkNotNull(name, "null String").trim();
		checkState(!this.name.isEmpty(), "empty String");
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		if (null != parentNames) {
			for (String parentName : parentNames) {
				builder.add(parentName.trim());
			}
		}
		this.parentNames = builder.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DefaultCategory)) {
			return false;
		}
		DefaultCategory that = (DefaultCategory) o;
		return Objects.equals(getName(), that.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName());
	}
}
