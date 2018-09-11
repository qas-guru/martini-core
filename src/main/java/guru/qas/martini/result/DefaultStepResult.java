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
	private final List<HttpEntity> embedded;

	private Long startTimestamp;
	private Long endTimestamp;
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
	public Optional<Status> getStatus() {
		return Optional.ofNullable(status);
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public Optional<Exception> getException() {
		return Optional.ofNullable(exception);
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	@Override
	public Optional<Long> getStartTimestamp() {
		return Optional.ofNullable(startTimestamp);
	}

	public void setStartTimestamp(Long l) {
		this.startTimestamp = l;
	}

	@Override
	public Optional<Long> getEndTimestamp() {
		return Optional.ofNullable(endTimestamp);
	}

	public void setEndTimestamp(Long l) {
		endTimestamp = l;
	}

	public DefaultStepResult(Step step, StepImplementation implementation) {
		this.step = checkNotNull(step, "null Step");
		this.implementation = implementation;
		this.embedded = Lists.newArrayList();
	}

	public void add(HttpEntity entity) {
		if (null != entity) {
			embedded.add(entity);
		}
	}

	@Override
	public Optional<Long> getExecutionTime(TimeUnit unit) {
		checkNotNull(unit, "null TimeUnit");

		Long millis = getStartTimestamp().isPresent() && getEndTimestamp().isPresent() ?
			getEndTimestamp().get() - getStartTimestamp().get() : null;
		Long conversion = null == millis ? null : unit.convert(millis, TimeUnit.MILLISECONDS);
		return Optional.ofNullable(conversion);
	}
}
