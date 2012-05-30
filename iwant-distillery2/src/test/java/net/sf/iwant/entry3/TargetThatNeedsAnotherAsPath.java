package net.sf.iwant.entry3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.io.StreamUtil;

class TargetThatNeedsAnotherAsPath implements Target {

	private final String name;
	private final Target ingredient;

	public TargetThatNeedsAnotherAsPath(String name, Target ingredient) {
		this.name = name;
		this.ingredient = ingredient;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public List<Target> ingredients() {
		return Collections.singletonList(ingredient);
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		StringBuilder content = new StringBuilder();
		String ingredientContent = StreamUtil.toString(new FileInputStream(
				ingredient.path(ctx)));
		content.append("Stream using '");
		content.append(ingredientContent);
		content.append("' as ingredient");
		return new ByteArrayInputStream(content.toString().getBytes());
	}

	@Override
	public File path(TargetEvaluationContext ctx) throws Exception {
		File path = ctx.freshPathTo(this);
		OutputStream out = new FileOutputStream(path);
		StreamUtil.pipe(content(ctx), out);
		out.close();
		return path;
	}

}