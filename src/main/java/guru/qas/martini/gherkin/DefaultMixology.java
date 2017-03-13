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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.Resource;

import com.google.common.collect.Lists;

import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultMixology implements Mixology {

	protected final Parser<GherkinDocument> parser;
	protected final Compiler compiler;

	@Autowired
	protected DefaultMixology(Parser<GherkinDocument> parser, Compiler compiler) {
		this.parser = parser;
		this.compiler = compiler;
	}

	@Override
	public Iterable<Recipe> get(Resource resource) throws IOException {
		checkNotNull(resource, "null Resource");

		URL location = resource.getURL();
		TokenMatcher matcher = new TokenMatcher();

		try (InputStream is = resource.getInputStream();
			 InputStreamReader isr = new InputStreamReader(is)
		) {
			GherkinDocument document = parser.parse(isr, matcher);
			return getRecipes(location, document);
		}
	}

	protected List<Recipe> getRecipes(URL location, GherkinDocument document) throws IOException {
		Feature feature = document.getFeature();
		String path = location.toExternalForm();
		List<Pickle> pickles = compiler.compile(document, path);
		return getRecipes(feature, pickles);
	}

	protected List<Recipe> getRecipes(Feature feature, Collection<Pickle> pickles) {
		ArrayList<Recipe> recipes = Lists.newArrayListWithExpectedSize(pickles.size());
		for (Pickle pickle : pickles) {
			Recipe recipe = new Recipe(feature, pickle);
			recipes.add(recipe);
		}
		return recipes;
	}


}
