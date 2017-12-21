package org.fluentjava.iwant.plugin.ant;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.taskdefs.Untar;
import org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;

public class Untarred extends Target {

	private final Path from;
	private final String compression;

	public Untarred(String name, Path from, String compression) {
		super(name);
		this.from = from;
		this.compression = compression;
	}

	public static UntarredSpex with() {
		return new UntarredSpex();
	}

	public static class UntarredSpex {

		private String name;
		private Path from;
		private String compression;

		public Untarred end() {
			return new Untarred(name, from, compression);
		}

		public UntarredSpex name(String name) {
			this.name = name;
			return this;
		}

		public UntarredSpex from(Path from) {
			this.from = from;
			return this;
		}

		public UntarredSpex gzCompression() {
			this.compression = "gzip";
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
		untarTo(ctx.cached(from), dest, compression);
	}

	public static void untarTo(File tar, File dest, String compression) {
		Untar untar = new Untar();
		untar.setDest(dest);
		untar.setSrc(tar);
		if (compression != null) {
			UntarCompressionMethod compressionMethod = new UntarCompressionMethod();
			compressionMethod.setValue(compression);
			untar.setCompression(compressionMethod);
		}

		untar.execute();
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":compression=" + compression
				+ ":" + ingredients();
	}

}
