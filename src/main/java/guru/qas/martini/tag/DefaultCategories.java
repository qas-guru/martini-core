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

package guru.qas.martini.tag;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import guru.qas.martini.Martini;

import static com.google.common.base.Preconditions.checkNotNull;

@Configurable
@SuppressWarnings("WeakerAccess")
public class DefaultCategories implements Categories, ApplicationContextAware, InitializingBean {

	protected static final String TAG_NAME = "Category";
	protected final Multimap<String, String> ascendingHierarchy;
	protected ApplicationContext applicationContext;

	public DefaultCategories() {
		ascendingHierarchy = HashMultimap.create();
	}

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
		this.applicationContext = checkNotNull(applicationContext, "null ApplicationContext");
	}

	@Override
	public void afterPropertiesSet() {
		Map<String, Category> beans = applicationContext.getBeansOfType(Category.class);
		Collection<Category> categories = beans.values();

		for (Category category : categories) {
			String name = category.getName();
			Iterable<String> parentNames = category.getParentNames();

			if (Iterables.isEmpty(parentNames)) {
				ascendingHierarchy.put(name, null);
			}
			else {
				for (String parentName : parentNames) {
					ascendingHierarchy.put(name, parentName);
				}
			}
		}
	}

	@Override
	public boolean isDefined(String classification) {
		checkNotNull(classification, "null String");
		return ascendingHierarchy.containsKey(classification) || ascendingHierarchy.containsValue(classification);
	}

	@Override
	public boolean isMatch(String classification, MartiniTag tag) {
		boolean evaluation = false;

		if (isCategory(tag)) {
			String argument = tag.getArgument();
			evaluation = classification.equals(argument);

			if (!evaluation && null != argument) {
				Set<String> ancestors = getAncestors(argument);
				evaluation = ancestors.contains(classification);
			}
		}
		return evaluation;
	}

	protected boolean isCategory(MartiniTag tag) {
		String name = tag.getName();
		return TAG_NAME.equals(name);
	}

	protected Set<String> getAncestors(String argument) {
		Collection<String> parents = ascendingHierarchy.get(argument);
		Set<String> ancestry = new LinkedHashSet<>();
		for (String parent : parents) {
			if (null != parent) {
				ancestry.add(parent);
				ancestry.addAll(getAncestors(parent));
			}
		}
		return ancestry;
	}

	@Override
	public Set<String> getCategorizations(Martini martini) {
		checkNotNull(martini, "null Martini");
		Collection<MartiniTag> tags = martini.getTags();

		Set<String> categories = new LinkedHashSet<>();

		for (MartiniTag tag : tags) {
			if (isCategory(tag)) {
				String argument = tag.getArgument();
				if (null != argument && !argument.isEmpty()) {
					Set<String> ancestors = getAncestors(argument);
					categories.add(argument);
					categories.addAll(ancestors);
				}
			}
		}
		return categories;
	}
}
