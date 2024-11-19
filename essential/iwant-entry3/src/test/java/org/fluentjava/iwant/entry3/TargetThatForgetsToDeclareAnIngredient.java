package org.fluentjava.iwant.entry3;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.entry.Iwant;

public class TargetThatForgetsToDeclareAnIngredient extends Target {

	private final Path implicitIngredient;

	public TargetThatForgetsToDeclareAnIngredient(String name,
			Path implicitIngredient) {
		super(name);
		this.implicitIngredient = implicitIngredient;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		// this reference shall cause a failure:
		File cachedIngredient = ctx.cached(implicitIngredient);
		// this shall never be run:
		Iwant.textFileEnsuredToHaveContentAndBeTouched(ctx.cached(this),
				cachedIngredient.getCanonicalPath());
	}

	@Override
	public List<Path> ingredients() {
		// here's the bug to test:
		return Collections.emptyList();
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + implicitIngredient;
	}

}
