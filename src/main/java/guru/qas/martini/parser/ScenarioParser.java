package guru.qas.martini.parser;

import java.io.IOException;

import org.springframework.core.io.Resource;

interface ScenarioParser {
	Iterable<Scenario> getScenarios(Resource resource) throws IOException;
}
