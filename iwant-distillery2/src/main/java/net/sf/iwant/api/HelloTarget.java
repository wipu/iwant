package net.sf.iwant.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.io.StreamUtil;

public class HelloTarget extends Target {

	private final String message;

	public HelloTarget(String name, String message) {
		super(name);
		this.message = message;
	}

	@Override
	public List<Path> ingredients() {
		return Collections.emptyList();
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) {
		return new ByteArrayInputStream(message.getBytes());
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File cachedContent = ctx.cached(this);
		StreamUtil.pipeAndClose(content(ctx), new FileOutputStream(
				cachedContent));
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + message;
	}

}