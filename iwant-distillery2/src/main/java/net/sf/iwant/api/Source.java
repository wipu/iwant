package net.sf.iwant.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
	public File cachedAt(CacheLocations cached) {
		return new File(cached.wsRoot(), name());
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		return new FileInputStream(ctx.freshPathTo(this));
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		// nothing to build for source
	}

	@Override
	public List<Path> ingredients() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public String contentDescriptor() {
		return null;
	}

}
