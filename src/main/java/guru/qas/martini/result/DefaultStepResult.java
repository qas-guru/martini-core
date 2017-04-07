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

package guru.qas.martini.result;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import gherkin.ast.Step;
import guru.qas.martini.event.Status;
import guru.qas.martini.step.StepImplementation;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class DefaultStepResult implements StepResult {

	private final Step step;
	private final StepImplementation implementation;
	private final ElapsedTime elapsedTime;
	private final List<HttpEntity> embedded;
	private Status status;
	private Exception exception;

	@Override
	public Step getStep() {
		return step;
	}

	@Override
	public StepImplementation getStepImplementation() {
		return implementation;
	}

	public List<HttpEntity> getEmbedded() {
		return ImmutableList.copyOf(embedded);
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	@Override
	public Long getStartTimestamp() {
		return elapsedTime.getStartTimestamp();
	}

	public void setStartTimestamp(Long timestamp) {
		elapsedTime.setStartTimestamp(timestamp);
	}

	@Override
	public Long getEndTimestamp() {
		return elapsedTime.getEndTimestamp();
	}

	public void setEndTimestamp(Long timestamp) {
		elapsedTime.setEndTimestamp(timestamp);
	}

	public DefaultStepResult(Step step, StepImplementation implementation) {
		this.step = checkNotNull(step, "null Step");
		this.implementation = implementation;
		this.embedded = Lists.newArrayList();
		this.elapsedTime = new ElapsedTime();
	}

	public void add(HttpEntity entity) {
		if (null != entity) {
			embedded.add(entity);
		}
	}

	@Override
	public Long getExecutionTime(TimeUnit unit) {
		checkNotNull(unit, "null TimeUnit");
		return elapsedTime.getExecutionTime(unit);
	}
}
