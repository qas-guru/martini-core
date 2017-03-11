package guru.qas.martini.gherkin;

import gherkin.ast.Feature;
import gherkin.pickles.Pickle;

@SuppressWarnings("WeakerAccess")
public class Recipe {

	protected final Feature feature;
	protected final Pickle pickle;

	public Feature getFeature() {
		return feature;
	}

	public Pickle getPickle() {
		return pickle;
	}

	protected Recipe(Feature feature, Pickle pickle) {
		this.feature = feature;
		this.pickle = pickle;
	}
}
