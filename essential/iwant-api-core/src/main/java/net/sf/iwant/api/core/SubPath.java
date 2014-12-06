package net.sf.iwant.api.core;

import java.io.File;
import java.io.InputStream;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.coreservices.FileUtil;

public class SubPath extends TargetBase {

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
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("parent", parent)
				.parameter("relativePath", relativePath).nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File from = new File(ctx.cached(parent), relativePath);
		FileUtil.copyRecursively(from, ctx.cached(this));
	}

	public Path parent() {
		return parent;
	}

	public String relativePath() {
		return relativePath;
	}

}
