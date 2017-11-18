package org.fluentjava.iwant.api.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;

public class Concatenated extends TargetBase {

	private final List<Fragment> fragments;

	private Concatenated(String name, List<Fragment> fragments) {
		super(name);
		this.fragments = fragments;
	}

	public static ConcatenatedBuilder named(String name) {
		return new ConcatenatedBuilder(name);
	}

	public static class ConcatenatedBuilder {

		private final List<Fragment> fragments = new ArrayList<>();
		private final String name;

		public ConcatenatedBuilder(String name) {
			this.name = name;
		}

		public ConcatenatedBuilder string(String string) {
			fragments.add(new StringFragment(string));
			return this;
		}

		public ConcatenatedBuilder bytes(int... bytes) {
			byte[] value = new byte[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				value[i] = (byte) bytes[i];
			}
			return bytes(value);
		}

		public ConcatenatedBuilder bytes(byte[] bytes) {
			fragments.add(new BytesFragment(bytes));
			return this;
		}

		public ConcatenatedBuilder contentOf(Path path) {
			fragments.add(new PathContentFragment(path));
			return this;
		}

		public ConcatenatedBuilder nativePathTo(Path path) {
			fragments.add(new NativePathFragment(path));
			return this;
		}

		public ConcatenatedBuilder unixPathTo(Path path) {
			fragments.add(new UnixPathFragment(path));
			return this;
		}

		public Concatenated end() {
			return new Concatenated(name, fragments);
		}

	}

	private static interface Fragment {

		void writeTo(OutputStream out, TargetEvaluationContext ctx)
				throws IOException;

		IngredientsAndParametersPlease ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse);

	}

	private static class StringFragment implements Fragment {

		private final String value;

		public StringFragment(String value) {
			this.value = value;
		}

		@Override
		public void writeTo(OutputStream out, TargetEvaluationContext ctx)
				throws IOException {
			PrintWriter writer = new PrintWriter(out);
			writer.write(value);
			writer.flush();
		}

		@Override
		public IngredientsAndParametersPlease ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			return iUse.parameter("string", value);
		}

	}

	private static class PathContentFragment implements Fragment {

		private final Path value;

		public PathContentFragment(Path value) {
			this.value = value;
		}

		@Override
		public void writeTo(OutputStream out, TargetEvaluationContext ctx)
				throws IOException {
			InputStream in = new FileInputStream(ctx.cached(value));
			try {
				ctx.iwant().pipe(in, out);
			} finally {
				in.close();
			}
		}

		@Override
		public IngredientsAndParametersPlease ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			return iUse.ingredients("content-of", value);
		}

	}

	private static class NativePathFragment implements Fragment {

		private final Path value;

		public NativePathFragment(Path value) {
			this.value = value;
		}

		@Override
		public void writeTo(OutputStream out, TargetEvaluationContext ctx)
				throws IOException {
			PrintWriter writer = new PrintWriter(out);
			writer.append(
					ctx.iwant().pathWithoutBackslashes(ctx.cached(value)));
			writer.flush();
		}

		@Override
		public IngredientsAndParametersPlease ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			return iUse.ingredients("native-path", value);
		}

	}

	private static class UnixPathFragment implements Fragment {

		private final Path value;

		public UnixPathFragment(Path value) {
			this.value = value;
		}

		@Override
		public void writeTo(OutputStream out, TargetEvaluationContext ctx)
				throws IOException {
			PrintWriter writer = new PrintWriter(out);
			writer.append(ctx.iwant().unixPathOf(ctx.cached(value)));
			writer.flush();
		}

		@Override
		public IngredientsAndParametersPlease ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			return iUse.ingredients("unix-path", value);
		}

	}

	private static class BytesFragment implements Fragment {

		private final byte[] value;

		public BytesFragment(byte[] value) {
			this.value = value;
		}

		@Override
		public void writeTo(OutputStream out, TargetEvaluationContext ctx)
				throws IOException {
			out.write(value);
		}

		@Override
		public IngredientsAndParametersPlease ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			return iUse.parameter("bytes", Arrays.toString(value));
		}

	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		IngredientsAndParametersPlease fUse = iUse;
		for (Fragment fragment : fragments) {
			fUse = fragment.ingredientsAndAttributes(fUse);
		}
		return fUse.nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		OutputStream out = new FileOutputStream(ctx.cached(this));
		try {
			for (Fragment fragment : fragments) {
				fragment.writeTo(out, ctx);
			}
		} finally {
			out.close();
		}
	}

}
