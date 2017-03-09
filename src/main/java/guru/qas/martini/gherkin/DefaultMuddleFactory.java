package guru.qas.martini.gherkin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.Resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import guru.qas.martini.MartiniException;

@Configurable
class DefaultMuddleFactory implements MuddleFactory {

	private final FeatureResourceLoader loader;
	private final Parser<GherkinDocument> parser;
	private final Compiler compiler;

	private final AtomicReference<ImmutableList<Muddle>> reference;

	@Autowired
	DefaultMuddleFactory(FeatureResourceLoader loader, Parser<GherkinDocument> parser, Compiler compiler) {
		this.loader = loader;
		this.parser = parser;
		this.compiler = compiler;
		reference = new AtomicReference<>();
	}

	@Override
	public List<Muddle> getMuddles() {
		synchronized (reference) {
			ImmutableList<Muddle> muddles = reference.get();
			return null == muddles ? loadMuddles() : muddles;
		}
	}

	private List<Muddle> loadMuddles() {
		try {
			Resource[] resources = loader.getResources();
			ImmutableList<Muddle> muddles = getMuddles(resources);
			reference.set(muddles);
			return muddles;
		} catch (IOException e) {
			throw new MartiniException("unable to load feature resources", e);
		}
	}

	private ImmutableList<Muddle> getMuddles(Resource[] resources) throws IOException {
		ImmutableList.Builder<Muddle> builder = ImmutableList.builder();
		for (Resource resource : resources) {
			List<Muddle> muddles = getMuddles(resource);
			builder.addAll(muddles);
		}
		return builder.build();
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
