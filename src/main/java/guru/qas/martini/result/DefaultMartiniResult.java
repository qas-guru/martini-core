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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import guru.qas.martini.Martini;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.event.Status;

import static com.google.common.base.Preconditions.*;

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

	@Override
	public Long getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(Long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	@Override
	public Long getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(Long endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	public Long getExecutionTimeMs() {
		Long executionTime = null;
		for (StepResult stepResult : stepResults) {
			Long elapsed = stepResult.getExecutionTime(TimeUnit.MILLISECONDS);
			executionTime = null == executionTime ? elapsed : null == elapsed ? executionTime : executionTime + elapsed;
		}
		return executionTime;
	}

	@Override
	public Exception getException() {
		Exception e = null;
		for (Iterator<StepResult> i = stepResults.iterator(); null == e && i.hasNext(); ) {
			StepResult stepResult = i.next();
			e = stepResult.getException();
		}
		return e;
	}

	protected DefaultMartiniResult(
		UUID id,
		SuiteIdentifier suiteIdentifier,
		Martini martini,
		Iterable<String> categorizations,
		String threadGroupName,
		String threadName
	) {
		this.id = id;
		this.suiteIdentifier = suiteIdentifier;
		this.martini = martini;
		this.categorizations = ImmutableSet.copyOf(categorizations);
		this.threadGroupName = threadGroupName;
		this.threadName = threadName;
		this.stepResults = new ArrayList<>();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected SuiteIdentifier suiteIdentifier;
		protected Martini martini;
		protected ImmutableSet<String> categorizations;
		protected String threadGroupName;
		protected String threadName;

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

		public Builder setCategorizations(Iterable<String> categorizations) {
			this.categorizations = null == categorizations ? ImmutableSet.of() : ImmutableSet.copyOf(categorizations);
			return this;
		}

		public Builder setThreadGroupName(String s) {
			this.threadGroupName = normalize(s);
			return this;
		}

		protected String normalize(String s) {
			return null == s ? null : s.trim();
		}

		public Builder setThreadName(String s) {
			this.threadName = normalize(s);
			return this;
		}

		public DefaultMartiniResult build() {
			checkState(null != suiteIdentifier, "null DefaultSuiteIdentifier");
			checkState(null != martini, "null Martini");
			checkState(null != threadGroupName && !threadGroupName.isEmpty(), "null or empty thread group name");
			checkState(null != threadName && !threadName.isEmpty(), "null or empty thread name");
			UUID id = UUID.randomUUID();
			return new DefaultMartiniResult(id, suiteIdentifier, martini, categorizations, threadGroupName, threadName);
		}
	}

	@Override
	public Status getStatus() {
		Status status = null;
		Set<Status> statii = Sets.newHashSet();
		for (StepResult stepResult : stepResults) {
			Status stepStatus = stepResult.getStatus();
			statii.add(stepStatus);
		}

		if (statii.contains(Status.FAILED)) {
			status = Status.FAILED;
		}
		else if (statii.contains(Status.SKIPPED)) {
			status = Status.SKIPPED;
		}
		else if (statii.contains(Status.PASSED)) {
			status = Status.PASSED;
		}
		return null == status ? Status.SKIPPED : status;

	}

	public void add(StepResult result) {
		checkNotNull(result, "null StepResult");
		stepResults.add(result);
	}
}
