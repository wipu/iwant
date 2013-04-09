package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;

public class ClassNameList extends Target {

	private final Path classes;
	private final StringFilter filter;

	public ClassNameList(String name, Path classes, StringFilter filter) {
		super(name);
		this.classes = classes;
		this.filter = filter;
	}

	public static ClassNameListSpex with() {
		return new ClassNameListSpex();
	}

	public static class ClassNameListSpex {

		private String name;
		private Path classes;
		private StringFilter filter;

		public ClassNameListSpex name(String name) {
			this.name = name;
			return this;
		}

		public ClassNameListSpex classes(Path classes) {
			this.classes = classes;
			return this;
		}

		public ClassNameListSpex matching(StringFilter filter) {
			this.filter = filter;
			return this;
		}

		public ClassNameList end() {
			return new ClassNameList(name, classes, filter);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		String packageName = "";
		File dir = ctx.cached(classes);
		List<String> fileNames = fileNamesUnder(dir, packageName);

		StringBuilder out = new StringBuilder();
		for (String fileName : fileNames) {
			String className = toClassName(fileName);
			if (isAcceptedByFilter(className)) {
				out.append(className).append("\n");
			}
		}

		Iwant.newTextFile(ctx.cached(this), out.toString());
	}

	private boolean isAcceptedByFilter(String className) {
		if (filter == null) {
			return true;
		}
		return filter.matches(className);
	}

	private static String toClassName(String fileName) {
		return fileName.replaceFirst("^/", "").replaceAll("/", ".")
				.replaceFirst(".class$", "");
	}

	private List<String> fileNamesUnder(File dir, String parentName) {
		List<String> classNames = new ArrayList<String>();
		File[] children = dir.listFiles();
		Arrays.sort(children);
		for (File child : children) {
			String childName = fullName(parentName, child.getName());
			if (child.isDirectory()) {
				classNames.addAll(fileNamesUnder(child, childName));
			} else {
				if (isFileAClassFile(child)) {
					classNames.add(childName);
				}
			}
		}
		return classNames;
	}

	private static boolean isFileAClassFile(File file) {
		return file.getName().endsWith(".class");
	}

	private static String fullName(String parentName, String name) {
		return parentName + "/" + name;
	}

	@Override
	public List<Path> ingredients() {
		return Arrays.asList(classes);
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");
		b.append("  classes:").append(classes).append("\n");
		if (filter != null) {
			b.append("  filter:").append(filter).append("\n");
		}
		b.append("}\n");
		return b.toString();
	}

}
