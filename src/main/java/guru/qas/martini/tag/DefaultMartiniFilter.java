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

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import guru.qas.martini.Martini;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DefaultMartiniFilter {

	protected static final String TAG_SMOKE = "@Smoke";
	protected static final String TAG_WIP = "@WIP";
	protected static final String TAG_CLASSIFICATION = "@Classification";

	protected final Martini martini;
	protected final Classifications classifications;

	public DefaultMartiniFilter(Martini martini, Classifications classifications) {
		this.martini = checkNotNull(martini, "null Martini");
		this.classifications = checkNotNull(classifications, "null Classifications");
	}

	// MUST be public for MethodReference to locate functionality.
	public boolean isSmoke() {
		return isTagged(TAG_SMOKE);
	}

	public boolean isWIP() {
		return isTagged(TAG_WIP);
	}

	// MUST be public for MethodReference to locate functionality.
	public boolean isTagged(String tagName) {
		List<MartiniTag> matches = getTags(tagName);
		return !matches.isEmpty();
	}

	public List<MartiniTag> getTags(String tagName) {
		String trimmed = tagName.trim();
		Iterable<MartiniTag> tags = martini.getTags();
		List<MartiniTag> matches = Lists.newArrayList();
		for (MartiniTag tag : tags) {
			String formatted = String.format("@%s", tag.getName());
			if (formatted.equals(tagName)) {
				matches.add(tag);
			}
		}
		return matches;
	}

	public boolean isClassified(String classification) {
		String trimmed = classification.trim();
		List<MartiniTag> tags = getTags(TAG_CLASSIFICATION);

		boolean evaluation = false;
		for (Iterator<MartiniTag> i = tags.iterator(); !evaluation && i.hasNext(); ) {
			MartiniTag martiniTag = i.next();
			evaluation = classifications.isMatch(classification, martiniTag);
		}
		return evaluation;
	}
}
