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

package guru.qas.martini;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.expression.Expression;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.collect.ImmutableList;

import guru.qas.martini.filter.category.CategoryResolver;
import guru.qas.martini.filter.feature.FeatureResolver;
import guru.qas.martini.filter.gated.GatedResolver;
import guru.qas.martini.filter.id.IdResolver;
import guru.qas.martini.filter.resource.ResourceResolver;
import guru.qas.martini.filter.scenario.ScenarioResolver;
import guru.qas.martini.tag.Categories;
import guru.qas.martini.filter.tag.TagResolver;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultMixologist implements Mixologist, ApplicationContextAware {

	protected final Categories categories;
	protected final MartiniFactory martiniFactory;

	protected ApplicationContext applicationContext;

	@Autowired
	protected DefaultMixologist(Categories categories, MartiniFactory martiniFactory) {
		this.categories = categories;
		this.martiniFactory = martiniFactory;
	}

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext context) {
		this.applicationContext = checkNotNull(context, "null ApplicationContext");
	}

	@Override
	public Collection<Martini> getMartinis(@Nullable String spelFilter) {
		String trimmed = null == spelFilter ? "" : spelFilter.trim();
		Expression expression = trimmed.isEmpty() ? null : getExpression(trimmed);
		return null == expression ? getMartinis() : getMartinis(expression);
	}

	protected Expression getExpression(@Nonnull String expressionString) {
		SpelExpressionParser parser = new SpelExpressionParser();
		return parser.parseExpression(expressionString);
	}

	@Override
	public Collection<Martini> getMartinis() {
		return martiniFactory.getMartinis();
	}

	protected Collection<Martini> getMartinis(Expression expression) {
		StandardEvaluationContext evaluationContext = getEvaluationContext();
		return getMartinis().stream().filter(martini -> {
			Boolean evaluation = expression.getValue(evaluationContext, martini, Boolean.class);
			return Boolean.TRUE.equals(evaluation);
		}).collect(Collectors.toList());
	}

	protected StandardEvaluationContext getEvaluationContext() {
		List<MethodResolver> resolvers = getMethodResolvers();
		StandardEvaluationContext context = new StandardEvaluationContext();
		resolvers.forEach(context::addMethodResolver);
		return context;
	}

	protected List<MethodResolver> getMethodResolvers() {
		return ImmutableList.of(
			new ScenarioResolver(),
			new CategoryResolver(categories),
			new GatedResolver(),
			new FeatureResolver(),
			new ResourceResolver(applicationContext),
			new IdResolver(),
			new TagResolver()
		);
	}
}
