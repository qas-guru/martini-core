package guru.qas.martini.annotation;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkState;

@Component
class StepsAnnotationProcessor implements BeanPostProcessor {

	private final String stepsCanonicalName;
	private final String givenCanonicalName;
	private final Set<Class> implementations;
	private final Set<String> expressions;
	private final Map<Pattern, StandardMethodMetadata> patternMap;

	@Autowired
	StepsAnnotationProcessor() {
		stepsCanonicalName = Steps.class.getCanonicalName();
		givenCanonicalName = Given.class.getCanonicalName();
		implementations = Sets.newHashSet();
		expressions = Sets.newHashSet();
		patternMap = Maps.newHashMap();
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		return bean;
	}

	private boolean isSteps(StandardAnnotationMetadata metadata) {
		boolean concrete = metadata.isConcrete();
		return concrete && metadata.isAnnotated(stepsCanonicalName);
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		StandardAnnotationMetadata metadata = getMetadata(bean);
		if (isSteps(metadata)) {
			process(metadata);
		}
		return bean;
	}

	private static StandardAnnotationMetadata getMetadata(Object bean) {
		Class<?> implementation = bean.getClass();
		return new StandardAnnotationMetadata(implementation);
	}

	//		Class<?> managedBeanClass = bean.getClass();
	//		ReflectionUtils.MethodCallback callback =
	//			new GivenCallback(beanFactory, bean);
	//		ReflectionUtils.doWithMethods(managedBeanClass, callback);

	private void process(StandardAnnotationMetadata metadata) {
		Class<?> implementation = metadata.getIntrospectedClass();
		boolean processed = implementations.contains(implementation);
		checkState(!processed, "more than one instance found of class %s", implementation);

		processGivens(metadata);
		implementations.add(implementation);
	}

	private void processGivens(StandardAnnotationMetadata metadata) {
		Set<MethodMetadata> data = metadata.getAnnotatedMethods(this.givenCanonicalName);
		for (MethodMetadata datum : data) {
			StandardMethodMetadata cast = StandardMethodMetadata.class.cast(datum);
			processGiven(cast);
		}
	}

	private void processGiven(StandardMethodMetadata metadata) {
		Map<String, Object> attributes = metadata.getAnnotationAttributes(this.givenCanonicalName);
		checkState(1 == attributes.size(),
			"@Given must have contain exactly one regex value but found %s",
			Joiner.on(", ").withKeyValueSeparator('=').join(attributes));

		Object o = attributes.get("value");
		checkState(null != o,
			"@Given must have contain exactly one regex value but found %s",
			Joiner.on(", ").withKeyValueSeparator('=').join(attributes));

		String regex = o.toString();
		checkState(!regex.isEmpty(),
			"@Given must have contain exactly one regex value but found %s",
			Joiner.on(", ").withKeyValueSeparator('=').join(attributes));

		checkState(!expressions.contains(regex), "More than one step uses regular expression %s.", regex);

		Pattern pattern = Pattern.compile(regex);
		patternMap.put(pattern, metadata);
	}

	@EventListener({ContextRefreshedEvent.class})
	private void contextRefreshedEvent() {
		implementations.clear();
		expressions.clear();
	}
}
