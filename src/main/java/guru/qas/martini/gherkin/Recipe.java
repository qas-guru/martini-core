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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Sets;

import gherkin.ast.Feature;
import gherkin.ast.Location;
import gherkin.ast.ScenarioDefinition;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("WeakerAccess")
public class Recipe {

	protected final Feature feature;
	protected final Pickle pickle;
	protected final AtomicReference<ScenarioDefinition> definitionRef;

	public Feature getFeature() {
		return feature;
	}

	public Pickle getPickle() {
		return pickle;
	}

	protected Recipe(Feature feature, Pickle pickle) {
		this.feature = feature;
		this.pickle = pickle;
		definitionRef = new AtomicReference<>();
	}

	public ScenarioDefinition getScenarioDefinition() {
		synchronized (definitionRef) {
			ScenarioDefinition definition = definitionRef.get();
			if (null == definition) {
				List<PickleLocation> locations = pickle.getLocations();
				Set<Integer> lines = Sets.newHashSetWithExpectedSize(locations.size());
				for (PickleLocation location : locations) {
					int line = location.getLine();
					lines.add(line);
				}
				List<ScenarioDefinition> definitions = feature.getChildren();
				for (Iterator<ScenarioDefinition> i = definitions.iterator(); null == definition && i.hasNext(); ) {
					ScenarioDefinition candidate = i.next();
					Location location = candidate.getLocation();
					int line = location.getLine();
					definition = lines.contains(line) ? candidate : null;
				}
				checkState(null != definition, "unable to locate ScenarioDefinition in Feature");
				definitionRef.set(definition);
			}
			return definition;
		}
	}
}
