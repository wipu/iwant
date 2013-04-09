package net.sf.iwant.api.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class ExternalSource implements Path {

	private final File file;
	private String name;

	public ExternalSource(File file) throws IOException {
		this.file = file;
		this.name = file.getCanonicalPath();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		System.err.println("TODO content");
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public File cachedAt(CacheScopeChoices cachedAt) {
		return file;
	}

	@Override
	public List<Path> ingredients() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return name;
	}

}
