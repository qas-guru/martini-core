package guru.qas.martini.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultScenarioParserTest {

	private DefaultScenarioParser parser;

	@BeforeClass
	public void setUpClass() {
		parser = new DefaultScenarioParser();
	}

	@AfterClass
	public void tearDownClass() {
		parser = null;
	}

	@Test(
		expectedExceptions = IllegalStateException.class,
		expectedExceptionsMessageRegExp = "keyword Feature missing in resource .*")
	public void testEmptyFile() throws IOException {
		ClassPathResource resource = new ClassPathResource("empty.feature");
		try (LineNumberReader lineNumberReader = getLineNumberReader(resource)) {
			parser.getFeature(resource, lineNumberReader);
		}
	}

	private LineNumberReader getLineNumberReader(ClassPathResource resource) throws IOException {
		InputStream inputStream = resource.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		return new LineNumberReader(inputStreamReader);
	}

	@Test
	public void testGetFeature() throws IOException {
		ClassPathResource resource = new ClassPathResource("simple.feature");
		try (LineNumberReader lineNumberReader = getLineNumberReader(resource)) {
			Feature feature = parser.getFeature(resource, lineNumberReader);
		}
	}
}
