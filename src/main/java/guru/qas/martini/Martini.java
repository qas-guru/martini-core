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

import java.util.Collection;
import java.util.Map;

import gherkin.ast.Step;
import gherkin.ast.Tag;
import gherkin.pickles.PickleTag;
import guru.qas.martini.gherkin.MartiniTag;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;

public interface Martini {

	Recipe getRecipe();

	Map<Step, StepImplementation> getStepIndex();

	Collection<Tag> getFeatureTags();

	Collection<PickleTag> getScenarioTags();

	/**
	 * @return both feature and scenario tags
	 */
	Collection<MartiniTag> getTags();
}