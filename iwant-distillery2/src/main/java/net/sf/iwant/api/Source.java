package net.sf.iwant.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class Source implements Target {

	private final String path;

	private Source(String path) {
		this.path = path;
	}

	public static Target underWsroot(String path) {
		return new Source(path);
	}

	@Override
	public String name() {
		return path;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		return new FileInputStream(path(ctx));
	}

	@Override
	public File path(TargetEvaluationContext ctx) throws Exception {
		return new File(ctx.wsRoot(), path);
	}

	@Override
	public List<Target> ingredients() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

}
