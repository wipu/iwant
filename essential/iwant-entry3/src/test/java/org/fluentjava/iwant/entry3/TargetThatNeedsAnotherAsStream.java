package org.fluentjava.iwant.entry3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.coreservices.StreamUtil;

class TargetThatNeedsAnotherAsStream extends Target {

	private final Path ingredient;

	public TargetThatNeedsAnotherAsStream(String name, Path ingredient) {
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
		content.append("Stream using '");
		content.append(StreamUtil.toString(ingredient.content(ctx)));
		content.append("' as ingredient");
		return new ByteArrayInputStream(content.toString().getBytes());
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File path = ctx.cached(this);
		try (OutputStream out = new FileOutputStream(path)) {
			StreamUtil.pipe(content(ctx), out);
		}
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredient;
	}

}