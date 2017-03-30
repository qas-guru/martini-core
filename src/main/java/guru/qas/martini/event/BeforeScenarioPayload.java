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

import guru.qas.martini.Martini;

@SuppressWarnings("WeakerAccess")
public class BeforeScenarioPayload extends MartiniEventPayload {

	private static final long serialVersionUID = 2311230281092387428L;

	protected final ScenarioIdentifier identifier;
	protected final Martini martini;

	public ScenarioIdentifier getScenarioIdentifier() {
		return identifier;
	}

	public Martini getMartini() {
		return martini;
	}

	public BeforeScenarioPayload(long timestamp, ScenarioIdentifier identifier, Martini martini) {
		super(timestamp);
		this.identifier = identifier;
		this.martini = martini;
	}
}
