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

package org.springframework.schema.beans;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

import com.google.common.base.Splitter;

import guru.qas.martini.tag.DefaultCategory;

public class MartiniCategoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class getBeanClass(Element element) {
		return DefaultCategory.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		String name = element.getAttribute("name");
		bean.addConstructorArgValue(name);

		String parents = element.getAttribute("parentNames");
		List<String> parentList = null == parents ? null :
			Splitter.on(',').trimResults().omitEmptyStrings().splitToList(parents);
		bean.addConstructorArgValue(null == parentList ? Collections.emptyList() : parentList);
	}

	@Override
	protected boolean shouldGenerateId() {
		return true;
	}
}

