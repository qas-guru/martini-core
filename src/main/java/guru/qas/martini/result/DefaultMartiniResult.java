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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import guru.qas.martini.Martini;
import guru.qas.martini.event.MartiniSuiteIdentifier;
import guru.qas.martini.event.Status;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class DefaultMartiniResult implements MartiniResult {

	private static final long serialVersionUID = 5018046850140682254L;

	protected final MartiniSuiteIdentifier suiteIdentifier;
	protected final Martini martini;
	protected final ImmutableSet<String> categorizations;
	protected final String threadGroupName;
	protected final String threadName;
	protected final List<StepResult> stepResults;
	protected final transient AtomicReference<ElapsedTime> elapsedTimeRef;
	protected final transient AtomicReference<Status> statusRef;

	public MartiniSuiteIdentifier getMartiniSuiteIdentifier() {
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

	protected DefaultMartiniResult(
		MartiniSuiteIdentifier suiteIdentifier, Martini martini,
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
		this.elapsedTimeRef = new AtomicReference<>();
		this.statusRef = new AtomicReference<>();
	}


	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected MartiniSuiteIdentifier suiteIdentifier;
		protected Martini martini;
		protected ImmutableSet<String> categorizations;
		protected String threadGroupName;
		protected String threadName;

		protected Builder() {
		}

		public Builder setMartiniSuiteIdentifier(MartiniSuiteIdentifier i) {
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
			checkState(null != suiteIdentifier, "null DefaultMartiniSuiteIdentifier");
			checkState(null != martini, "null Martini");
			checkState(null != threadGroupName && !threadGroupName.isEmpty(), "null or empty thread group name");
			checkState(null != threadName && !threadName.isEmpty(), "null or empty thread name");
			return new DefaultMartiniResult(suiteIdentifier, martini, categorizations, threadGroupName, threadName);
		}
	}

	@Override
	public Status getStatus() {
		Status status;
		synchronized (statusRef) {
			status = statusRef.get();

			if (null == status) {
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
				status = null == status ? Status.SKIPPED : status;
				statusRef.set(status);
			}
		}
		return status;
	}

	public void add(StepResult result) {
		checkNotNull(result, "null StepResult");
		stepResults.add(result);
	}

	@Override
	public Long getStartTimestamp() {
		ElapsedTime elapsed = getElapsedTime();
		return elapsed.getStartTimestamp();
	}

	protected ElapsedTime getElapsedTime() {
		ElapsedTime elapsedTime;
		synchronized (elapsedTimeRef) {
			elapsedTime = elapsedTimeRef.get();
			if (null == elapsedTime) {
				elapsedTime = new ElapsedTime();
				StepResult first = stepResults.isEmpty() ? null : stepResults.get(0);
				Long startTimestamp = null == first ? null : first.getStartTimestamp();
				elapsedTime.setStartTimestamp(startTimestamp);

				Long endTimestamp = null;
				if (null != first) {
					int size = stepResults.size();
					for (int i = size; null == endTimestamp && i > 0; i--) {
						StepResult result = stepResults.get(size - 1);
						endTimestamp = result.getEndTimestamp();
					}
				}
				elapsedTime.setEndTimestamp(endTimestamp);
				elapsedTimeRef.set(elapsedTime);
			}
		}
		return elapsedTime;
	}

	@Override
	public Long getEndTimestamp() {
		ElapsedTime elapsed = getElapsedTime();
		return elapsed.getEndTimestamp();
	}

	@Override
	public Long getExecutionTime(TimeUnit unit) {
		checkNotNull(unit, "null TimeUnit");
		ElapsedTime elapsed = getElapsedTime();
		return elapsed.getExecutionTime(unit);
	}
}
