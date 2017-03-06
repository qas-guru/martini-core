package guru.qas.martini.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.Resource;

@Configurable
class DefaultScenarioParser implements ScenarioParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultScenarioParser.class);

	protected final Charset charset;

	@Autowired
	DefaultScenarioParser(Charset charset) {
		this.charset = charset;
	}

	@Override
	public Iterable<Scenario> getScenarios(Resource resource) throws IOException {

		try (
			InputStream inputStream = resource.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			LineNumberReader lineNumberReader = new LineNumberReader(inputStreamReader)
		) {
			Feature feature = getFeature(resource, lineNumberReader);
			LOGGER.debug("{parsed Feature: }", feature);
		}

		throw new UnsupportedOperationException();
	}

	protected Feature getFeature(Resource resource, LineNumberReader reader) throws IOException {
		FeatureLineProcessor processor = new FeatureLineProcessor(resource);

		String line = null;
		do {
			reader.mark(Integer.MAX_VALUE);
			line = reader.readLine();
		} while (null != line && processor.processLine(line));

		reader.reset();
		return processor.getResult();
	}
}
