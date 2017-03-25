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

package guru.qas.martini.scope;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import guru.qas.martini.Martini;
import guru.qas.martini.event.AfterScenarioEvent;
import guru.qas.martini.event.BeforeScenarioEvent;

@Component
public class ScenarioScopeListener {

	private final ScenarioScope scope;

	@Autowired
	public ScenarioScopeListener(ScenarioScope scope) {
		this.scope = scope;
	}

	@EventListener
	public void handle(BeforeScenarioEvent event) {
		scope.clear();
		Martini martini = event.getMartini();
		Thread thread = Thread.currentThread();
		ThreadGroup threadGroup = thread.getThreadGroup();
		String groupName = threadGroup.getName();
		String name = thread.getName();
		String id = String.format("%s %s %s", groupName, name, martini);
		scope.setConversationId(id);
	}

	@EventListener
	public void handle(AfterScenarioEvent event) {
		scope.clear();
	}
}
