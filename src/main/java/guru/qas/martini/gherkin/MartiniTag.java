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

package guru.qas.martini.gherkin;

import gherkin.ast.Tag;
import gherkin.pickles.PickleTag;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class MartiniTag {

	protected final String name;

	public String getName() {
		return name;
	}

	protected MartiniTag(String name) {
		this.name = name;
	}

	public static Builder builder() {
		return new Builder();
	}

	@SuppressWarnings("WeakerAccess")
	public static class Builder {

		protected Builder() {
		}

		public MartiniTag build(Tag featureTag) {
			checkNotNull(featureTag, "null Tag");
			String name = featureTag.getName();
			return new MartiniTag(name);
		}

		public MartiniTag build(PickleTag pickleTag) {
			checkNotNull(pickleTag, "null PickleTag");
			String name = pickleTag.getName();
			return new MartiniTag(name);
		}
	}
}
