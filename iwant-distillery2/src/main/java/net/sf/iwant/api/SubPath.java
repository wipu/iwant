package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.coreservices.FileUtil;

public class SubPath extends Target {

	private final Path parent;
	private final String relativePath;

	public SubPath(String name, Path parent, String relativePath) {
		super(name);
		this.parent = parent;
		this.relativePath = relativePath;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		return Arrays.asList(parent);
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File from = new File(ctx.cached(parent), relativePath);
		FileUtil.copyRecursively(from, ctx.cached(this));
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + parent + ":"
				+ relativePath;
	}

	public Path parent() {
		return parent;
	}

	public String relativePath() {
		return relativePath;
	}

}
