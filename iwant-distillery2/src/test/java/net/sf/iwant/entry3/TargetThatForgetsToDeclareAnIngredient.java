package net.sf.iwant.entry3;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.api.Path;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;

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
		Iwant.writeTextFile(ctx.cached(this),
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
