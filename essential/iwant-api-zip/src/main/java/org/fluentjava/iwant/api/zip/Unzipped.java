package org.fluentjava.iwant.api.zip;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.taskdefs.Expand;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;

public class Unzipped extends Target {

	private final Path from;

	public Unzipped(String name, Path from) {
		super(name);
		this.from = from;
	}

	public static UnzippedSpex with() {
		return new UnzippedSpex();
	}

	public static class UnzippedSpex {

		private String name;
		private Path from;

		public Unzipped end() {
			return new Unzipped(name, from);
		}

		public UnzippedSpex name(String name) {
			this.name = name;
			return this;
		}

		public UnzippedSpex from(Path from) {
			this.from = from;
			return this;
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		return Arrays.asList(from);
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);

		unzipTo(ctx.cached(from), dest);
	}

	public static void unzipTo(File from, File to) {
		Expand untar = new Expand();
		untar.setDest(to);
		untar.setSrc(from);

		untar.execute();
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

	public Path from() {
		return from;
	}

}
