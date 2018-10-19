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

package guru.qas.martini.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import guru.qas.martini.event.AfterScenarioEvent;
import guru.qas.martini.event.AfterSuiteEvent;
import guru.qas.martini.event.BeforeScenarioEvent;
import guru.qas.martini.result.MartiniResult;
import guru.qas.martini.scope.MartiniScenarioScope;

@Component
public class ScenarioScopeListener {

	private MartiniScenarioScope scope;

	@Autowired
	public ScenarioScopeListener(MartiniScenarioScope scope) {
		this.scope = scope;
	}

	@EventListener
	public void handle(BeforeScenarioEvent event) {
		scope.clear();
		MartiniResult result = event.getPayload();
		scope.setScenarioIdentifier(result);
	}

	@EventListener
	public void handle(@SuppressWarnings("unused") AfterScenarioEvent ignored) {
		scope.clear();
	}

	@EventListener
	public void handle(@SuppressWarnings("unused") AfterSuiteEvent event) {
		scope.clear();
	}
}
