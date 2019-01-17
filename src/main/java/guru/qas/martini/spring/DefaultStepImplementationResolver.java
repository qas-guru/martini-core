/*
Copyright 2018-2019 Penny Rohr Curich

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

package guru.qas.martini.spring;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

import gherkin.ast.Step;
import guru.qas.martini.step.StepImplementationResolver;
import guru.qas.martini.step.exception.AmbiguousStepException;
import guru.qas.martini.step.DefaultStepImplementation;
import guru.qas.martini.step.StepImplementation;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
@Lazy
@Component
public class DefaultStepImplementationResolver implements StepImplementationResolver, ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
		checkNotNull(applicationContext, "null ApplicationContext");
		this.applicationContext = applicationContext;
	}

	public StepImplementation getImplementation(@Nonnull Step step) {
		checkNotNull(step, "null Step");

		Collection<StepImplementation> matches = getMatches(step);

		StepImplementation match;

		int count = matches.size();
		if (1 == count) {
			match = Iterables.getOnlyElement(matches);
		}
		else if (count > 1) {
			throw AmbiguousStepException.builder().setStep(step).setMatches(matches).build();
		}
		else {
			match = getUnimplemented(step);
		}
		return match;
	}

	protected Collection<StepImplementation> getMatches(Step step) {
		Map<String, StepImplementation> beans =
			applicationContext.getBeansOfType(StepImplementation.class, false, false);
		Collection<StepImplementation> candidates = beans.values();

		return candidates.stream()
			.filter(i -> i.isMatch(step))
			.collect(Collectors.toList());
	}

	protected StepImplementation getUnimplemented(Step step) {
		String keyword = step.getKeyword();
		return new DefaultStepImplementation(keyword);
	}
}
