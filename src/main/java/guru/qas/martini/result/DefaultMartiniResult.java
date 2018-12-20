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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

import guru.qas.martini.Martini;
import guru.qas.martini.event.Status;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.tag.Categories;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DefaultMartiniResult implements ModifiableMartiniResult {

	protected final UUID id;
	protected final SuiteIdentifier suiteIdentifier;
	protected final Martini martini;
	protected final ImmutableSet<String> categorizations;
	protected final String threadGroupName;
	protected final String threadName;
	protected final List<StepResult> stepResults;

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

	@Override
	public void add(StepResult result) {
		checkNotNull(result, "null StepResult");
		stepResults.add(result);
	}

	@Override
	public Optional<Status> getStatus() {
		return stepResults.stream()
			.map(result -> result.getStatus().orElse(null))
			.filter(Objects::nonNull)
			.distinct()
			.max(Ordering.natural());
	}

	@Override
	public Optional<Long> getStartTimestamp() {
		return stepResults.stream()
			.map(result -> result.getStartTimestamp().orElse(null))
			.filter(Objects::nonNull)
			.distinct()
			.min(Ordering.natural());
	}

	@Override
	public Optional<Long> getEndTimestamp() {
		return stepResults.stream()
			.map(result -> result.getEndTimestamp().orElse(null))
			.filter(Objects::nonNull)
			.distinct()
			.max(Ordering.natural());
	}

	@Override
	public Optional<Long> getExecutionTimeMs() {
		Long evaluation = null;
		if (getStartTimestamp().isPresent() && getEndTimestamp().isPresent()) {
			evaluation = getEndTimestamp().get() - getStartTimestamp().get();
		}
		return Optional.ofNullable(evaluation);
	}

	@Override
	public Optional<Exception> getException() {
		return stepResults.stream()
			.map(result -> result.getException().orElse(null))
			.filter(Objects::nonNull)
			.findFirst();
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

			Set<String> categorizations = categories.getCategorizations(martini);

			Thread thread = Thread.currentThread();
			String threadName = thread.getName();

			ThreadGroup threadGroup = thread.getThreadGroup();
			String threadGroupName = threadGroup.getName();

			return new DefaultMartiniResult(suiteIdentifier, martini, categorizations, threadGroupName, threadName);
		}
	}
}
