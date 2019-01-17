/*
Copyright 2017-2019 Penny Rohr Curich

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
import java.util.Map;
import java.util.Set;

import org.springframework.core.ResolvableType;

import com.google.common.collect.Lists;

import gherkin.ast.Step;
import guru.qas.martini.Martini;
import exception.MartiniException;
import guru.qas.martini.result.MartiniResult;
import guru.qas.martini.result.StepResult;
import guru.qas.martini.step.StepImplementation;

import static guru.qas.martini.event.StepEventMessages.*;

public class BeforeStepEvent extends MartiniScenarioEvent {

	public BeforeStepEvent(Object source, MartiniResult payload) {
		super(source, payload);
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClass(BeforeStepEvent.class);
	}

	public Step getStep() {
		List<Step> steps = getSteps();
		int index = getStepIndex();
		return getStep(steps, index);
	}

	private List<Step> getSteps() throws MartiniException {
		Martini martini = getMartini();
		Map<Step, StepImplementation> stepIndex = martini.getStepIndex();
		if (stepIndex.isEmpty()) {
			throw new MartiniException(EMPTY_STEPS, martini.getId());
		}
		Set<Step> steps = stepIndex.keySet();
		return Lists.newArrayList(steps);
	}

	private Martini getMartini() {
		MartiniResult martiniResult = super.getPayload();
		return martiniResult.getMartini();
	}

	private int getStepIndex() {
		MartiniResult payload = super.getPayload();
		List<StepResult> stepResults = payload.getStepResults();
		return stepResults.size();
	}

	private Step getStep(List<Step> steps, int index) {
		int stepCount = steps.size();
		if (index < 0 || index > stepCount - 1) {
			Martini martini = getMartini();
			String id = martini.getId();
			throw new MartiniException(STEP_RESULT_COUNT_MISMATCH, id, stepCount, index);
		}
		return steps.get(index);
	}
}
