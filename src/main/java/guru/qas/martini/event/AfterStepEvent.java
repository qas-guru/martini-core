/*
Copyright 2019 Penny Rohr Curich

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

import java.util.List;

import org.springframework.core.ResolvableType;

import com.google.common.collect.Iterables;

import gherkin.ast.Step;
import guru.qas.martini.Martini;
import guru.qas.martini.MartiniException;
import guru.qas.martini.result.MartiniResult;
import guru.qas.martini.result.StepResult;

public class AfterStepEvent extends MartiniScenarioEvent {

	public AfterStepEvent(Object source, MartiniResult payload) {
		super(source, payload);
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClass(AfterStepEvent.class);
	}

	public Step getStep() {
		MartiniResult payload = super.getPayload();
		List<StepResult> stepResults = payload.getStepResults();
		StepResult last = Iterables.getLast(stepResults, null);
		if (null == last) {
			Martini martini = payload.getMartini();
			String id = martini.getId();
			throw new MartiniException(StepEventMessages.EMPTY_RESULTS, id);
		}
		return last.getStep();
	}
}
