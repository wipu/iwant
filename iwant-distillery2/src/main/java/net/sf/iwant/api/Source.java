package net.sf.iwant.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class Source implements Path {

	private final String path;

	private Source(String path) {
		this.path = path;
	}

	public static Source underWsroot(String path) {
		return new Source(path);
	}

	@Override
	public String name() {
		return path;
	}

	@Override
	public File cachedAt(CacheScopeChoices cachedAt) {
		return cachedAt.source(this);
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		return new FileInputStream(ctx.cached(this));
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		// nothing to build for source
	}

	@Override
	public List<Path> ingredients() {
		return Collections.emptyList();
	}

	@Override
	public String contentDescriptor() {
		return null;
	}

	@Override
	public String toString() {
		return path;
	}

}
