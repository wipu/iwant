package net.sf.iwant.api.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.api.target.TargetBase;

public class HelloTarget extends TargetBase {

	private final String message;

	public HelloTarget(String name, String message) {
		super(name);
		this.message = message;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) {
		return new ByteArrayInputStream(message.getBytes());
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File cachedContent = ctx.cached(this);
		ctx.iwant().pipeAndClose(content(ctx),
				new FileOutputStream(cachedContent));
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.parameter("message", message).nothingElse();
	}

}