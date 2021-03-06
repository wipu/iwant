package org.fluentjava.iwant.entry3;

import java.io.File;

import org.fluentjava.iwant.api.model.IwantCoreServices;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.entry.Iwant;

public class IngredientCheckingTargetEvaluationContext
		implements TargetEvaluationContext {

	private final Target target;
	private final TargetEvaluationContext delegate;

	public IngredientCheckingTargetEvaluationContext(Target target,
			TargetEvaluationContext delegate) {
		this.target = target;
		this.delegate = delegate;
	}

	@Override
	public File wsRoot() {
		return delegate.wsRoot();
	}

	@Override
	public File cached(Path path) {
		if (!isLegalReference(path)) {
			throw new Iwant.IwantException("Target " + target + " referred to "
					+ path + " without " + "declaring it an ingredient.");
		}
		return delegate.cached(path);
	}

	private boolean isLegalReference(Path path) {
		// TODO this logic can be simplified
		if (equals(target, path)) {
			return true;
		}
		if (isDirectOrIndirectIngredientOf(path, target)) {
			return true;
		}
		return false;
	}

	private static boolean isDirectOrIndirectIngredientOf(
			Path ingredientCandidate, Path target) {
		for (Path ingredient : target.ingredients()) {
			if (ingredient == null) {
				throw new Iwant.IwantException(
						"Path '" + target + "' has a null ingredient.");
			}
			if (equals(ingredientCandidate, ingredient)) {
				return true;
			}
			if (isDirectOrIndirectIngredientOf(ingredientCandidate,
					ingredient)) {
				return true;
			}
		}
		return false;
	}

	private static boolean equals(Path p1, Path p2) {
		return p2.name().equals(p1.name());
	}

	@Override
	public IwantCoreServices iwant() {
		return delegate.iwant();
	}

	@Override
	public File freshTemporaryDirectory() {
		return delegate.freshTemporaryDirectory();
	}

}
