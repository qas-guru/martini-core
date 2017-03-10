package guru.qas.martini;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import gherkin.pickles.Pickle;
import gherkin.pickles.PickleStep;
import guru.qas.martini.annotation.GivenCallback;
import guru.qas.martini.annotation.StepsAnnotationProcessor;
import guru.qas.martini.gherkin.Muddle;
import guru.qas.martini.gherkin.MuddleFactory;

import static com.google.common.base.Preconditions.checkState;

@Service
public class Bartender {

	private final MuddleFactory muddleFactory;
	private final StepsAnnotationProcessor processor;

	Bartender(MuddleFactory muddleFactory, StepsAnnotationProcessor processor) {
		this.muddleFactory = muddleFactory;
		this.processor = processor;
	}

	public void doSomething() {
		List<Muddle> muddles = muddleFactory.getMuddles();

		GivenCallback callback = processor.getGivenCallback();
		Map<Pattern, Method> index = callback.getMethodIndex();

		for (Muddle muddle : muddles) {
			Pickle pickle = muddle.getPickle();
			List<PickleStep> steps = pickle.getSteps();
			for (PickleStep step : steps) {
				String text = step.getText();
				List<Pattern> matches = Lists.newArrayList();
				for (Pattern pattern : index.keySet()) {
					Matcher matcher = pattern.matcher(text);
					if (matcher.find()) {
						matches.add(pattern);
					}
				}

				checkState(matches.size() < 2,
					"ambiguous step for regex %s, matches %s", text, Joiner.on(", ").join(matches));

				System.out.println("breakpoint");
			}

		}
		// Now, find all the steps.
		// And match the steps to the muddles to create Cocktails.
		// Muddle, Pattern and Method.
	}
}
