package guru.qas.martini;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.pickles.Compiler;

import gherkin.ast.GherkinDocument;
import gherkin.pickles.Pickle;

@Service
public class Bartender {

	public void doSomething() throws IOException {
		System.out.println("hello");

		AstBuilder astBuilder = new AstBuilder();
		Parser<GherkinDocument> parser = new Parser<>(astBuilder);
		TokenMatcher matcher = new TokenMatcher();
		Compiler compiler = new Compiler();

		ClassPathResource resource = new ClassPathResource("/sample.feature");
		try (InputStream is = resource.getInputStream();
			 InputStreamReader isr = new InputStreamReader(is)
		) {
			GherkinDocument document = parser.parse(isr, matcher);
			//Feature feature = document.getFeature();
			String path = resource.getURL().toExternalForm();
			List<Pickle> pickles = compiler.compile(document, path);

			// Document has comments and descriptions.
			// Pickle has actual resource location and incorporates parent steps.
			// Marry the two on name/location for complete information.
			// Give that thing a name (mixer, like for cocktails?)
			// FILTER scenarios we don't want
			// Then remaining things married to step implementations will be known as a Martini.

			System.out.println("breakpoint");
			// Location is missing the resource it came from
		}

	}
}
