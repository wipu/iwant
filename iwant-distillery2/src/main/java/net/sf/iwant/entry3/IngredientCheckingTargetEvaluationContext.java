package net.sf.iwant.entry3;

import java.io.File;

import net.sf.iwant.api.Path;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;

public class IngredientCheckingTargetEvaluationContext implements
		TargetEvaluationContext {

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
					+ path + " without " + "declaring it as an ingredient.");
		}
		return delegate.cached(path);
	}

	private boolean isLegalReference(Path path) {
		if (target.name().equals(path.name())) {
			return true;
		}
		if (target.ingredients().contains(path)) {
			return true;
		}
		return false;
	}

	@Override
	public Iwant iwant() {
		return delegate.iwant();
	}

}
