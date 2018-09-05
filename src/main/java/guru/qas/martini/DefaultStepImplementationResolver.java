/*
Copyright 2018 Penny Rohr Curich

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

package guru.qas.martini;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.Iterables;

import gherkin.ast.Step;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.AmbiguousStepException;
import guru.qas.martini.step.StepImplementation;
import guru.qas.martini.step.UnimplementedStep;
import guru.qas.martini.step.UnimplementedStepException;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultStepImplementationResolver implements StepImplementationResolver {

	private final Recipe recipe;

	private Collection<StepImplementation> implementations;
	private boolean unimplementedStepsFatal;

	@Autowired
	void setUnimplementedStepsFatal(@Value("${unimplemented.steps.fatal:#{false}}") boolean b) {
		this.unimplementedStepsFatal = b;
	}

	@Autowired
	void setStepImplementations(Collection<StepImplementation> implementations) {
		this.implementations = implementations;
		/*
				Map<String, StepImplementation> beans = context.getBeansOfType(StepImplementation.class, false, false);
		Collection<StepImplementation> implementations = beans.values();
		 */
	}

	protected DefaultStepImplementationResolver(Recipe recipe) {
		this.recipe = checkNotNull(recipe, "null Recipe");
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
			throw new AmbiguousStepException.Builder().setStep(step).setMatches(matches).build();
		}
		else if (unimplementedStepsFatal) {
			throw new UnimplementedStepException.Builder().setRecipe(recipe).setStep(step).build();
		}
		else {
			match = getUnimplemented(step);
		}
		return match;
	}

	protected Collection<StepImplementation> getMatches(Step step) {
		return implementations.stream()
			.filter(i -> i.isMatch(step))
			.collect(Collectors.toList());
	}

	protected UnimplementedStep getUnimplemented(Step step) {
		String keyword = step.getKeyword();
		return new UnimplementedStep(null == keyword ? "" : keyword.trim());
	}
}
