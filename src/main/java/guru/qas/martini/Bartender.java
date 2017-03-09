package guru.qas.martini;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.Feature;
import gherkin.pickles.Compiler;

import gherkin.ast.GherkinDocument;
import gherkin.pickles.Pickle;
import guru.qas.martini.feature.FeatureResourceLoader;

@Service
public class Bartender implements InitializingBean {

	private final FeatureResourceLoader loader;
	private Parser<GherkinDocument> parser;
	private Compiler compiler;

	public Bartender(FeatureResourceLoader loader) {
		this.loader = loader;

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		AstBuilder astBuilder = new AstBuilder();
		parser = new Parser<>(astBuilder);
		compiler = new Compiler();
	}

	public void getMuddles() throws IOException {
		Resource[] resources = loader.getResources();
		List<Muddle> muddles = getMuddles(resources);
		System.out.println("breakpoint");
	}

	private List<Muddle> getMuddles(Resource[] resources) throws IOException {
		List<Muddle> muddles = Lists.newArrayList();
		for (Resource resource : resources) {
			List<Muddle> resourceMuddles = getMuddles(resource);
			muddles.addAll(resourceMuddles);
		}
		return muddles;
	}

	private List<Muddle> getMuddles(Resource resource) throws IOException {
		URL location = resource.getURL();
		TokenMatcher matcher = new TokenMatcher();

		try (InputStream is = resource.getInputStream();
			 InputStreamReader isr = new InputStreamReader(is)
		) {
			GherkinDocument document = parser.parse(isr, matcher);
			return getMuddles(location, document);
		}
	}

	private List<Muddle> getMuddles(URL location, GherkinDocument document) throws IOException {
		Feature feature = document.getFeature();
		String path = location.toExternalForm();
		List<Pickle> pickles = compiler.compile(document, path);
		return getMuddles(feature, pickles);
	}

	private List<Muddle> getMuddles(Feature feature, Collection<Pickle> pickles) {
		ArrayList<Muddle> muddles = Lists.newArrayListWithExpectedSize(pickles.size());
		for (Pickle pickle : pickles) {
			Muddle muddle = new Muddle(feature, pickle);
			muddles.add(muddle);
		}
		return muddles;
	}
}
