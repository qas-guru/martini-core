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

package guru.qas.martini.runtime.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import guru.qas.martini.event.AfterScenarioEvent;
import guru.qas.martini.event.AfterStepEvent;
import guru.qas.martini.event.AfterSuiteEvent;
import guru.qas.martini.event.BeforeScenarioEvent;
import guru.qas.martini.event.BeforeStepEvent;
import guru.qas.martini.event.BeforeSuiteEvent;
import guru.qas.martini.event.MartiniEventPublisher;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.result.MartiniResult;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultEventManager implements EventManager {

	protected final MartiniEventPublisher publisher;

	@Autowired
	DefaultEventManager(MartiniEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void publishBeforeSuite(Object source, SuiteIdentifier suiteIdentifier) {
		BeforeSuiteEvent event = new BeforeSuiteEvent(source, suiteIdentifier);
		publisher.publish(event);
	}

	@Override
	public void publishAfterSuite(Object source, SuiteIdentifier suiteIdentifier) {
		AfterSuiteEvent event = new AfterSuiteEvent(source, suiteIdentifier);
		publisher.publish(event);
	}

	@Override
	public void publishBeforeScenario(Object source, MartiniResult result) {
		BeforeScenarioEvent event = new BeforeScenarioEvent(source, result);
		publisher.publish(event);
	}

	@Override
	public void publishAfterScenario(Object source, MartiniResult result) {
		AfterScenarioEvent event = new AfterScenarioEvent(source, result);
		publisher.publish(event);
	}

	@Override
	public void publishBeforeStep(Object source, MartiniResult result) {
		BeforeStepEvent event = new BeforeStepEvent(source, result);
		publisher.publish(event);
	}

	@Override
	public void publishAfterStep(Object source, MartiniResult result) {
		AfterStepEvent event = new AfterStepEvent(source, result);
		publisher.publish(event);
	}
}

