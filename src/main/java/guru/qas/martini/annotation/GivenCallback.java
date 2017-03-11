package guru.qas.martini.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Sets;

import guru.qas.martini.step.DefaultGivenStep;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class GivenCallback implements ReflectionUtils.MethodCallback {

	protected final ConfigurableListableBeanFactory beanFactory;
	protected final AtomicInteger atomicInteger;
	protected final Set<String> regularExpressions;

	protected GivenCallback(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.atomicInteger = new AtomicInteger();
		this.regularExpressions = Sets.newHashSet();
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		checkNotNull(method, "null Method");
		processGiven(method);
		processGivenContainer(method);
	}

	/*
	    @Override
    public Object postProcessBeforeInitialization(final Object bean, String name) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                // make the field accessible if defined private
                ReflectionUtils.makeAccessible(field);
                if (field.getAnnotation(Log.class) != null) {
                    Logger log = LoggerFactory.getLogger(bean.getClass());
                    field.set(bean, log);
                }
            }
        });
        return bean;
    }
	 */

	protected void processGiven(Method method) {
		Given annotation = AnnotationUtils.findAnnotation(method, Given.class);
		if (null != annotation) {
			process(method, annotation);
		}
	}

	protected void process(Method method, Given annotation) {
		checkState(Modifier.isPublic(method.getModifiers()), "Method is not public: %s", method);
		String regex = annotation.value().trim();
		checkState(!regex.isEmpty(), "@Given requires non-empty regex values.");
		checkState(!regularExpressions.contains(regex), "Multiple methods found for @Given regex \"%s\"", regex);
		Pattern pattern = Pattern.compile(regex);
		regularExpressions.add(regex);

		String name = String.format("given%s", atomicInteger.getAndIncrement());

		DefaultGivenStep step = new DefaultGivenStep(pattern, method);
		beanFactory.registerSingleton(name, step);
	}

	protected void processGivenContainer(Method method) {
		GivenContainer container = AnnotationUtils.findAnnotation(method, GivenContainer.class);
		if (null != container) {
			Given[] annotations = container.value();
			for (Given annotation : annotations) {
				process(method, annotation);
			}
		}
	}
}
