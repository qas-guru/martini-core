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
import gherkin.ast.GherkinDocument;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;

@Service
public class Martini {

	public void doSomething() throws IOException {
		System.out.println("hello");

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
		TokenMatcher matcher = new TokenMatcher();
		Compiler compiler = new Compiler();
		List<Pickle> pickles = new ArrayList<>();

		ClassPathResource resource = new ClassPathResource("/sample.feature");
		try (InputStream is = resource.getInputStream();
			 InputStreamReader isr = new InputStreamReader(is)
		) {
			GherkinDocument document = parser.parse(isr, matcher);
			String resourcePath = resource.getPath();
			List<Pickle> pickle = compiler.compile(document, resourcePath);
			pickles.addAll(pickle);
		}

		System.out.println("breakpoint");
	}
}
