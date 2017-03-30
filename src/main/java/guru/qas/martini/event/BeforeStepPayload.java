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

package guru.qas.martini.event;

import gherkin.ast.Step;
import guru.qas.martini.Martini;

@SuppressWarnings("WeakerAccess")
public abstract class BeforeStepPayload extends MartiniEventPayload {

	private static final long serialVersionUID = 7133271133173172948L;

	protected final ScenarioIdentifier scenarioIdentifier;
	protected final Martini martini;
	protected final Step step;

	public ScenarioIdentifier getScenarioIdentifier() {
		return scenarioIdentifier;
	}

	public Martini getMartini() {
		return martini;
	}

	public Step getStep() {
		return step;
	}

	public BeforeStepPayload(long timestamp, ScenarioIdentifier scenarioIdentifier, Martini martini, Step step) {
		super(timestamp);
		this.scenarioIdentifier = scenarioIdentifier;
		this.martini = martini;
		this.step = step;
	}
}
