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

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;

import gherkin.ast.Background;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class Recipe implements Serializable {

	private static final long serialVersionUID = 1388547503104118707L;

	protected final Resource source;
	protected final Feature feature;
	protected final Pickle pickle;
	protected final PickleLocation location;
	protected final ScenarioDefinition definition;

	public Resource getSource() {
		return source;
	}

	public Feature getFeature() {
		return feature;
	}

	public Pickle getPickle() {
		return pickle;
	}

	public PickleLocation getLocation() {
		return location;
	}

	public ScenarioDefinition getScenarioDefinition() {
		return definition;
	}

	protected Recipe(
		Resource source,
		Feature feature,
		Pickle pickle,
		PickleLocation location,
		ScenarioDefinition definition) {
		this.source = source;
		this.feature = feature;
		this.pickle = pickle;
		this.location = location;
		this.definition = definition;
	}

	public Background getBackground() {
		List<ScenarioDefinition> children = feature.getChildren();
		List<Background> backgrounds = children.stream().filter(Background.class::isInstance).map(Background.class::cast).collect(Collectors.toList());
		checkState(backgrounds.isEmpty() || 1 == backgrounds.size(), "more than one Background identified");
		return backgrounds.isEmpty() ? null : backgrounds.get(0);
	}

	public String getId() {
		String featureName = getFeature().getName();
		String scenarioName = getPickle().getName();
		int line = getLocation().getLine();
		String formatted = String.format("%s:%s:%s", featureName, scenarioName, line);
		return formatted.replaceAll("\\s+", "_");
	}
}
