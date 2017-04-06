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
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import guru.qas.martini.Martini;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.event.Status;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class DefaultMartiniResult implements MartiniResult {

	private static final long serialVersionUID = 5018046850140682254L;

	protected final SuiteIdentifier suiteIdentifier;
	protected final Martini martini;
	protected final ImmutableSet<String> categorizations;
	protected final String threadGroupName;
	protected final String threadName;
	protected final List<StepResult> stepResults;

	protected Long startTimestamp;
	protected Long endTimestamp;
	protected Long executionTimeMs;

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
		return executionTimeMs;
	}

	public void setExecutionTimeMs(Long executionTimeMs) {
		this.executionTimeMs = executionTimeMs;
	}

	protected DefaultMartiniResult(
		SuiteIdentifier suiteIdentifier, Martini martini,
		Iterable<String> categorizations,
		String threadGroupName,
		String threadName
	) {
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
			return new DefaultMartiniResult(suiteIdentifier, martini, categorizations, threadGroupName, threadName);
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
