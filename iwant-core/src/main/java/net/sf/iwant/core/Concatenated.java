package net.sf.iwant.core;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Concatenated implements Content {

	private final List<Fragment> fragments;

	private Concatenated(List<Fragment> fragments) {
		this.fragments = fragments;
	}

	public static ConcatenatedBuilder from() {
		return new ConcatenatedBuilder();
	}

	public static class ConcatenatedBuilder {

		private final List<Fragment> fragments = new ArrayList();

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
			fragments.add(new PathFragment(path));
			return this;
		}

		public Concatenated end() {
			return new Concatenated(fragments);
		}

	}

	private static interface Fragment {

		void writeTo(FileWriter out, RefreshEnvironment refresh)
				throws IOException;

	}

	private static class StringFragment implements Fragment {

		private final String value;

		public StringFragment(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "string:'" + value + "'";
		}

		public void writeTo(FileWriter out, RefreshEnvironment refresh)
				throws IOException {
			out.write(value);
		}

	}

	private static class PathFragment implements Fragment {

		private final Path value;

		public PathFragment(Path value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "path:" + value;
		}

		public void writeTo(FileWriter out, RefreshEnvironment refresh)
				throws IOException {
			FileReader in = new FileReader(value.asAbsolutePath(refresh
					.locations()));
			// TODO optimize if needed
			while (true) {
				int c = in.read();
				if (c < 0) {
					in.close();
					return;
				}
				out.write(c);
			}
		}

	}

	private static class BytesFragment implements Fragment {

		private final byte[] value;

		public BytesFragment(byte[] value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "bytes:" + Arrays.toString(value);
		}

		public void writeTo(FileWriter out, RefreshEnvironment refresh)
				throws IOException {
			for (int c : value) {
				out.write(c);
			}
		}

	}

	public SortedSet<Path> ingredients() {
		SortedSet<Path> ingredients = new TreeSet();
		for (Fragment fragment : fragments) {
			if (fragment instanceof PathFragment) {
				PathFragment pathFragment = (PathFragment) fragment;
				ingredients.add(pathFragment.value);
			}
		}
		return ingredients;
	}

	public void refresh(RefreshEnvironment refresh) throws Exception {
		FileWriter out = new FileWriter(refresh.destination());
		for (Fragment fragment : fragments) {
			fragment.writeTo(out, refresh);
		}
		out.close();
	}

	public String definitionDescription() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getSimpleName()).append(" {\n");
		for (Fragment fragment : fragments) {
			b.append(fragment).append("\n");
		}
		b.append("}\n");
		return b.toString();
	}

}
