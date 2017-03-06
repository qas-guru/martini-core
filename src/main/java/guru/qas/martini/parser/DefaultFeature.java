package guru.qas.martini.parser;

import org.springframework.core.io.Resource;

import static com.google.common.base.Preconditions.checkState;

class DefaultFeature implements Feature {

	protected Resource resource;
	protected String title;

	public Resource getResource() {
		return resource;
	}

	public String getTitle() {
		return title;
	}

	protected DefaultFeature(String title) {
		this.title = title;

	}

	static Builder builder() {
		return new Builder();
	}

	static final class Builder {

		private String title;

		private Builder() {
		}

		boolean isTitleSet() {
			return null != title;
		}

		Builder setTitle(String s) {
			this.title = s;
			return this;
		}

		DefaultFeature build(Resource resource) {
			checkState(isTitleSet(), "keyword Feature missing in resource %s", resource);
			return new DefaultFeature(title);
		}
	}
}
