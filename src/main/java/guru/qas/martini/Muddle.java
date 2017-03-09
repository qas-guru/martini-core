package guru.qas.martini;

import gherkin.ast.Feature;
import gherkin.pickles.Pickle;

public class Muddle {

	private final Feature feature;
	private final Pickle pickle;

	public Feature getFeature() {
		return feature;
	}

	public Pickle getPickle() {
		return pickle;
	}

	public Muddle(Feature feature, Pickle pickle) {
		this.feature = feature;
		this.pickle = pickle;
	}
}
