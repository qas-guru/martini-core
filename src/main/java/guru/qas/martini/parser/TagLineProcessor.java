package guru.qas.martini.parser;

import java.io.IOException;

import com.google.common.io.LineProcessor;

class TagLineProcessor implements LineProcessor<Tag> {

	@Override
	public boolean processLine(String s) throws IOException {
		return false;
	}

	@Override
	public Tag getResult() {
		return null;
	}
}
