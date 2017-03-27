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

import java.util.List;

import com.google.common.collect.Lists;

import guru.qas.martini.gherkin.MartiniTag;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DefaultMartiniFilter {

	private static final String TAG_SMOKE = "@Smoke";
	private static final String TAG_WIP = "@WIP";

	protected final Martini martini;

	protected DefaultMartiniFilter(Martini martini) {
		this.martini = martini;
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
}
