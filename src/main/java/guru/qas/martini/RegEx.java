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

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.collect.Lists;

import gherkin.ast.Feature;
import gherkin.ast.Location;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Tag;

@SuppressWarnings("WeakerAccess")
public class RegEx {

	private final Feature feature;

	public RegEx() {
		Location tagLocation = new Location(1, 1);
		List<Tag> tags = Lists.newArrayList(new Tag(tagLocation, "Smoke"), new Tag(tagLocation, "Fire"));
		Location featureLocation = new Location(2, 1);
		String language = "en";
		String keyword = "Feature";
		String name = "New Subsystem";
		String description = "";
		List<ScenarioDefinition> children = Lists.newArrayList();
		feature = new Feature(tags, featureLocation, language, keyword, name, description, children);
	}

	public boolean isSmoke() {
		List<Tag> tags = feature.getTags();
		boolean match = false;
		for (Iterator<Tag> i = tags.iterator(); !match && i.hasNext();) {
			Tag tag = i.next();
			match = tag.getName().trim().equals("Smoke");
		}
		return match;
	}

	public boolean isFire() {
		List<Tag> tags = feature.getTags();
		boolean match = false;
		for (Iterator<Tag> i = tags.iterator(); !match && i.hasNext();) {
			Tag tag = i.next();
			match = tag.getName().trim().equals("Fire");
		}
		return match;
	}

	public boolean isSmoke(String blah) {
		System.out.println("isSmoke(String) with argument " + blah);
		return true;
	}

	public static void main(String[] args) {
		StandardEvaluationContext ctx = new StandardEvaluationContext();
		ctx.setRootObject(new RegEx());


		SpelExpressionParser parser = new SpelExpressionParser();
		Expression expression = parser.parseExpression("isSmoke() and (isSmoke() and isFire())");
		Object value = expression.getValue(ctx);
		System.out.println("value: " + value);
	}
}
