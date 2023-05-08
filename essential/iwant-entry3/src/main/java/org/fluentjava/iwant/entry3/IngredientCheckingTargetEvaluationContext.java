package org.fluentjava.iwant.entry3;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.fluentjava.iwant.api.model.IwantCoreServices;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.entry.Iwant;

public class IngredientCheckingTargetEvaluationContext
		implements TargetEvaluationContext {

	private final Target target;
	private final TargetEvaluationContext delegate;
	private final ReferenceLegalityCheckCache refLegalityCheckCache;

	public IngredientCheckingTargetEvaluationContext(Target target,
			TargetEvaluationContext delegate,
			ReferenceLegalityCheckCache refLegalityCheckCache) {
		this.target = target;
		this.delegate = delegate;
		this.refLegalityCheckCache = refLegalityCheckCache;
	}

	public static class ReferenceLegalityCheckCache {

		private final Map<String, IngredientUsers> usersByName = new HashMap<>();

		private synchronized IngredientUsers usersOf(Path path) {
			IngredientUsers users = usersByName.get(path.name());
			if (users == null) {
				users = new IngredientUsers();
				usersByName.put(path.name(), users);
			}
			return users;
		}

	}

	private static class IngredientUsers {
		private final Map<String, Boolean> checkedUserNames = new HashMap<>();

		synchronized Boolean cachedResult(Path userCandidate) {
			return checkedUserNames.get(userCandidate.name());
		}

		synchronized void cacheResult(Path path, boolean isUser) {
			checkedUserNames.put(path.name(), isUser);
		}

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

	private boolean isDirectOrIndirectIngredientOf(Path ingredientCandidate,
			Path target) {
		IngredientUsers usersOfIngredientCandidate = refLegalityCheckCache
				.usersOf(ingredientCandidate);
		Boolean cachedResult = usersOfIngredientCandidate.cachedResult(target);
		if (cachedResult != null) {
			return cachedResult;
		}
		for (Path ingredient : target.ingredients()) {
			if (ingredient == null) {
				throw new Iwant.IwantException(
						"Path '" + target + "' has a null ingredient.");
			}
			if (equals(ingredientCandidate, ingredient)) {
				usersOfIngredientCandidate.cacheResult(target, true);
				return true;
			}
			if (isDirectOrIndirectIngredientOf(ingredientCandidate,
					ingredient)) {
				// no caching needed here, it was handled in the leaf of our
				// recursive call above:
				return true;
			}
		}
		usersOfIngredientCandidate.cacheResult(target, false);
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
