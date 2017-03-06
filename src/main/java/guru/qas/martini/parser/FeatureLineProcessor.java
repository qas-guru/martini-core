package guru.qas.martini.parser;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;

import com.google.common.collect.Lists;
import com.google.common.io.LineProcessor;

import static com.google.common.base.Preconditions.checkNotNull;

class FeatureLineProcessor implements LineProcessor<Feature> {
	private static final Pattern PATTERN_FEATURE = Pattern.compile("^Feature:\\s*(.*)$");

	protected final Resource resource;
	private final List<String> buffer;

	protected String title;

	FeatureLineProcessor(Resource resource) {
		this.resource = resource;
		buffer = Lists.newArrayList();
	}

	@Override
	public boolean processLine(String s) throws IOException {
		checkNotNull(s, "null String");

		String trimmed = s.trim();
		Matcher matcher = PATTERN_FEATURE.matcher(trimmed);
		if (matcher.find()) {
			title = matcher.group(1);
		} else {
			buffer.add(trimmed);
		}
		return null == title;
	}

	@Override
	public Feature getResult() {
		throw new UnsupportedOperationException();
	}
}
