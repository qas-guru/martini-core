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

import java.util.Collection;

import org.apache.http.HttpEntity;

import com.google.common.collect.ImmutableList;

import gherkin.ast.Step;
import guru.qas.martini.Martini;

@SuppressWarnings("WeakerAccess")
public class AfterStepPayload extends MartiniEventPayload {

	private static final long serialVersionUID = -971472982661102859L;

	protected final ScenarioIdentifier scenarioIdentifier;
	protected final Martini martini;
	protected final Step step;
	protected final Status status;
	protected final Exception exception;
	protected final ImmutableList<HttpEntity> embedded;

	public ScenarioIdentifier getScenarioIdentifier() {
		return scenarioIdentifier;
	}

	public Martini getMartini() {
		return martini;
	}

	public Step getStep() {
		return step;
	}

	public Status getStatus() {
		return status;
	}

	public Exception getException() {
		return exception;
	}

	public ImmutableList<HttpEntity> getEmbedded() {
		return embedded;
	}

	public AfterStepPayload(
		long timestamp,
		ScenarioIdentifier scenarioIdentifier,
		Martini martini,
		Step step,
		Status status,
		Exception exception,
		Collection<HttpEntity> embedded
	) {
		super(timestamp);
		this.scenarioIdentifier = scenarioIdentifier;
		this.martini = martini;
		this.step = step;
		this.status = status;
		this.exception = exception;
		this.embedded = null == embedded ? null : ImmutableList.copyOf(embedded);
	}
}
