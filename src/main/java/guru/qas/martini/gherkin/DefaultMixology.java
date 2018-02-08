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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.Resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Location;
import gherkin.ast.ScenarioDefinition;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;

import static com.google.common.base.Preconditions.*;

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
		TokenMatcher matcher = new TokenMatcher();

		try (InputStream is = resource.getInputStream();
			 InputStreamReader isr = new InputStreamReader(is)
		) {
			GherkinDocument document = parser.parse(isr, matcher);
			return getRecipes(resource, document);
		}
	}

	protected List<Recipe> getRecipes(Resource source, GherkinDocument document) throws IOException {
		Feature feature = document.getFeature();
		FeatureWrapper featureWrapper = new FeatureWrapper(feature, source);
		List<Pickle> pickles = compiler.compile(document);
		return getRecipes(featureWrapper, pickles);
	}

	protected List<Recipe> getRecipes(FeatureWrapper feature, Collection<Pickle> pickles) {
		Map<Integer, Recipe> recipeIndex = new LinkedHashMap<>();

		RangeMap<Integer, ScenarioDefinition> rangeMap = getRangeMap(feature);
		for (Pickle pickle : pickles) {
			List<PickleLocation> locations = pickle.getLocations();
			Map<Integer, PickleLocation> locationIndex = new HashMap<>();
			locations.forEach(l -> {
				int line = l.getLine();
				locationIndex.put(line, l);
			});

			Integer line = Ordering.natural().max(locationIndex.keySet());
			recipeIndex.computeIfAbsent(line, l -> {
				PickleLocation location = locationIndex.get(l);
				Range<Integer> range = Range.singleton(line);
				RangeMap<Integer, ScenarioDefinition> subRangeMap = rangeMap.subRangeMap(range);
				Map<Range<Integer>, ScenarioDefinition> asMap = subRangeMap.asMapOfRanges();
				checkState(1 == asMap.size(), "no single range found encompassing PickleLocation %s", location);
				ScenarioDefinition definition = Iterables.getOnlyElement(asMap.values());
				return new Recipe(feature, pickle, location, definition);
			});
		}
		return ImmutableList.copyOf(recipeIndex.values());
	}

	protected RangeMap<Integer, ScenarioDefinition> getRangeMap(FeatureWrapper feature) {
		List<ScenarioDefinition> children = Lists.newArrayList(feature.getChildren());

		ImmutableRangeMap.Builder<Integer, ScenarioDefinition> builder = ImmutableRangeMap.builder();
		while (!children.isEmpty()) {
			ScenarioDefinition child = children.remove(0);
			Location location = child.getLocation();
			Integer childStart = location.getLine();

			ScenarioDefinition sibling = children.isEmpty() ? null : children.get(0);
			Location siblingLocation = null == sibling ? null : sibling.getLocation();
			Integer siblingStart = null == siblingLocation ? null : siblingLocation.getLine();

			Range<Integer> range = null == siblingStart ? Range.atLeast(childStart) : Range.closedOpen(childStart, siblingStart);
			builder.put(range, child);
		}
		return builder.build();
	}
}
