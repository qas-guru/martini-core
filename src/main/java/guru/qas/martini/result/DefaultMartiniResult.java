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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import guru.qas.martini.Martini;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.event.Status;
import guru.qas.martini.tag.Categories;

import static com.google.common.base.Preconditions.*;
import static guru.qas.martini.event.Status.PASSED;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DefaultMartiniResult implements MartiniResult {

	protected final UUID id;
	protected final SuiteIdentifier suiteIdentifier;
	protected final Martini martini;
	protected final ImmutableSet<String> categorizations;
	protected final String threadGroupName;
	protected final String threadName;
	protected final List<StepResult> stepResults;

	protected Long startTimestamp;
	protected Long endTimestamp;
	protected Status status;
	protected Exception exception;
	protected AtomicLong executionTimeMs;

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public SuiteIdentifier getSuiteIdentifier() {
		return suiteIdentifier;
	}

	@Override
	public Martini getMartini() {
		return martini;
	}

	@Override
	public String getThreadGroupName() {
		return threadGroupName;
	}

	@Override
	public String getThreadName() {
		return threadName;
	}

	public Set<String> getCategorizations() {
		return categorizations;
	}

	@Override
	public List<StepResult> getStepResults() {
		return ImmutableList.copyOf(stepResults);
	}

	public void setStartTimestamp(Long l) {
		this.startTimestamp = l;
	}

	@Override
	public Optional<Long> getStartTimestamp() {
		return Optional.ofNullable(startTimestamp);
	}

	public void setEndTimestamp(Long l) {
		this.endTimestamp = l;
	}

	@Override
	public Optional<Long> getEndTimestamp() {
		return Optional.ofNullable(endTimestamp);
	}

	@Override
	public Optional<Exception> getException() {
		return Optional.ofNullable(exception);
	}

	@Override
	public Optional<Long> getExecutionTimeMs() {
		return null == this.executionTimeMs ? Optional.empty() : Optional.of(executionTimeMs.get());
	}

	@Override
	public Optional<Status> getStatus() {
		return Optional.ofNullable(status);
	}

	protected DefaultMartiniResult(
		SuiteIdentifier suiteIdentifier,
		Martini martini,
		Iterable<String> categorizations,
		String threadGroupName,
		String threadName
	) {
		this.id = UUID.randomUUID();
		this.suiteIdentifier = suiteIdentifier;
		this.martini = martini;
		this.categorizations = ImmutableSet.copyOf(categorizations);
		this.threadGroupName = threadGroupName;
		this.threadName = threadName;
		this.stepResults = new ArrayList<>();
	}

	public void add(StepResult result) {
		checkNotNull(result, "null StepResult");
		stepResults.add(result);
		updateException(result);
		updateExecutionTime(result);
		updateStatus(result);
	}

	protected void updateException(StepResult result) {
		result.getException().ifPresent(exception ->
			this.exception = null == this.exception ? exception : this.exception);
	}

	protected void updateExecutionTime(StepResult result) {
		result.getExecutionTime(TimeUnit.MILLISECONDS).ifPresent(executionTime -> {
			if (null == this.executionTimeMs) {
				this.executionTimeMs = new AtomicLong(0);
			}
			this.executionTimeMs.getAndAdd(executionTime);
		});
	}

	protected void updateStatus(StepResult result) {
		result.getStatus().ifPresent(status -> {
			if (null == this.status || PASSED.equals(this.status)) {
				this.status = status;
			}
		});
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected SuiteIdentifier suiteIdentifier;
		protected Martini martini;

		protected Builder() {
		}

		public Builder setMartiniSuiteIdentifier(SuiteIdentifier i) {
			this.suiteIdentifier = i;
			return this;
		}

		public Builder setMartini(Martini martini) {
			this.martini = martini;
			return this;
		}

		protected String normalize(String s) {
			return null == s ? null : s.trim();
		}

		public DefaultMartiniResult build(Categories categories) {
			checkNotNull(categories, "null Categories");
			checkState(null != suiteIdentifier, "null DefaultSuiteIdentifier");
			checkState(null != martini, "null Martini");

			Thread thread = Thread.currentThread();
			String threadName = thread.getName();

			ThreadGroup threadGroup = thread.getThreadGroup();
			String threadGroupName = threadGroup.getName();

			Set<String> categorizations = categories.getCategorizations(martini);

			return new DefaultMartiniResult(suiteIdentifier, martini, categorizations, threadGroupName, threadName);
		}
	}
}
