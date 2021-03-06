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

package guru.qas.martini.result;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;

import gherkin.ast.Step;
import guru.qas.martini.event.Status;
import guru.qas.martini.step.StepImplementation;

public interface StepResult {

	Step getStep();

	StepImplementation getStepImplementation();

	List<HttpEntity> getEmbedded();

	Optional<Status> getStatus();

	Optional<Exception> getException();

	Optional<Long> getStartTimestamp();

	Optional<Long> getEndTimestamp();

	Optional<Long> getExecutionTime(TimeUnit unit);
}
