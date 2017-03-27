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

package guru.qas.martini;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

import gherkin.ast.Tag;
import guru.qas.martini.gherkin.MartiniTag;

@SuppressWarnings("WeakerAccess")
public class DefaultMartiniFilter {

	private static final String TAG_SMOKE = "Smoke";

	protected final Martini martini;

	protected DefaultMartiniFilter(Martini martini) {
		this.martini = martini;
	}

	protected boolean isSmoke() {
		return isTagged(TAG_SMOKE);

	}

	protected boolean isTagged(String tagName) {
		String trimmed = tagName.trim();
		Iterable<MartiniTag> tags = martini.getTags();
		boolean match = false;
		for (Iterator<MartiniTag> i = tags.iterator(); !match && i.hasNext(); ) {
			MartiniTag tag = i.next();
			String candidateTagName = tag.getName();
			match = trimmed.equals(candidateTagName);
		}
		return match;
	}
}
