package net.sf.iwant.entry3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.api.Path;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.io.StreamUtil;

class TargetThatNeedsAnotherAsPath extends Target {

	private final Path ingredient;

	public TargetThatNeedsAnotherAsPath(String name, Path ingredient) {
		super(name);
		this.ingredient = ingredient;
	}

	@Override
	public List<Path> ingredients() {
		return Collections.singletonList(ingredient);
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		StringBuilder content = new StringBuilder();
		String ingredientContent = StreamUtil.toString(new FileInputStream(ctx
				.freshPathTo(ingredient)));
		content.append("Stream using '");
		content.append(ingredientContent);
		content.append("' as ingredient");
		return new ByteArrayInputStream(content.toString().getBytes());
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File path = ctx.freshPathTo(this);
		OutputStream out = new FileOutputStream(path);
		StreamUtil.pipe(content(ctx), out);
		out.close();
	}

}