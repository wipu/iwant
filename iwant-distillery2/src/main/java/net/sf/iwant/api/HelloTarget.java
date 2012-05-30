package net.sf.iwant.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.io.StreamUtil;

public class HelloTarget implements Target {

	private final String name;
	private final String message;

	public HelloTarget(String name, String message) {
		this.name = name;
		this.message = message;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public List<Target> ingredients() {
		return Collections.emptyList();
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) {
		return new ByteArrayInputStream(message.getBytes());
	}

	@Override
	public File path(TargetEvaluationContext ctx) throws Exception {
		File cachedContent = ctx.freshPathTo(this);
		FileOutputStream out = new FileOutputStream(cachedContent);
		StreamUtil.pipe(content(ctx), out);
		out.close();
		return cachedContent;
	}

}