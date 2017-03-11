package guru.qas.martini;

import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import guru.qas.martini.gherkin.Mixology;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.gherkin.GherkinResourceLoader;

@SuppressWarnings("WeakerAccess")
@Service
public class DefaultMixologist implements Mixologist, InitializingBean {

	protected final GherkinResourceLoader loader;
	protected final Mixology mixology;

	@Autowired
	protected DefaultMixologist(GherkinResourceLoader loader, Mixology mixology) {
		this.loader = loader;
		this.mixology = mixology;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Resource[] resources = loader.getFeatureResources();
		initialize(resources);
	}

	protected void initialize(Resource[] resources) throws IOException {
		for (Resource resource : resources) {
			initialize(resource);
		}
	}

	protected void initialize(Resource resource) throws IOException {
		Iterable<Recipe> recipes = mixology.get(resource);
		initialize(recipes);
	}

	protected void initialize(Iterable<Recipe> recipes) {
		for (Recipe recipe : recipes) {
			initialize(recipe);
		}
	}

	protected void initialize(Recipe recipe) {
		System.out.println("Got a recipe: " + recipe);
	}

	public void doSomething() {
		System.out.println("breakpoint");
	}

//	public void doSomething() {
//		List<Recipe> mixology = this.mixology.getMuddles();
//
//		GivenCallback callback = processor.getGivenCallback();
//		Map<Pattern, Method> index = callback.getMethodIndex();
//
//		for (Recipe muddle : mixology) {
//			Pickle pickle = muddle.getPickle();
//			List<PickleStep> steps = pickle.getSteps();
//			for (PickleStep step : steps) {
//				String text = step.getText();
//				List<Pattern> matches = Lists.newArrayList();
//				for (Pattern pattern : index.keySet()) {
//					Matcher matcher = pattern.matcher(text);
//					if (matcher.find()) {
//						matches.add(pattern);
//					}
//				}
//
//				checkState(matches.size() < 2,
//					"ambiguous step for regex %s, matches %s", text, Joiner.on(", ").join(matches));
//
//				// TODO: check capturing groups against number of expected parameters in method
//				System.out.println("breakpoint");
//			}
//
//		}
//		// Now, find all the steps.
//		// And match the steps to the mixology to create Cocktails.
//		// Recipe, Pattern and Method.
//	}
}
