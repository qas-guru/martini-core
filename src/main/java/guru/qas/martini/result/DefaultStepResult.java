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

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class DefaultStepResult implements StepResult {

	private static final long serialVersionUID = -4054028161690659412L;

	private final Step step; // TODO: this thing is not serializable
	private final ElapsedTime elapsedTime;
	private final List<HttpEntity> embedded;
	private Status status;
	private Exception exception;

	@Override
	public Step getStep() {
		return step;
	}

	public List<HttpEntity> getEmbedded() {
		return ImmutableList.copyOf(embedded);
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public Long getStartTimestamp() {
		return elapsedTime.getStartTimestamp();
	}

	@Override
	public Long getEndTimestamp() {
		return elapsedTime.getEndTimestamp();
	}

	public DefaultStepResult(Step step) {
		this.step = checkNotNull(step, "null Step");
		this.embedded = Lists.newArrayList();
		this.elapsedTime = new ElapsedTime();
	}

	public void add(HttpEntity entity) {
		embedded.add(entity);
	}

	public void setStartTimestamp(Long timestamp) {
		elapsedTime.setStartTimestamp(timestamp);
	}

	public void setEndtimestamp(Long timestamp) {
		elapsedTime.setEndTimestamp(timestamp);
	}

	@Override
	public Long getExecutionTime(TimeUnit unit) {
		checkNotNull(unit, "null TimeUnit");
		return elapsedTime.getExecutionTime(unit);
	}
}
