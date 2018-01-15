package org.fluentjava.iwant.api.core;

import java.io.File;
import java.io.InputStream;

import org.fluentjava.iwant.api.model.CacheScopeChoices;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;

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
		// nothing to do
	}

	@Override
	public File cachedAt(CacheScopeChoices cachedAt) {
		return new File(parent.cachedAt(cachedAt), relativePath);
	}

	@Override
	public boolean expectsCachedTargetMissingBeforeRefresh() {
		return false;
	}

	public Path parent() {
		return parent;
	}

	public String relativePath() {
		return relativePath;
	}

}
