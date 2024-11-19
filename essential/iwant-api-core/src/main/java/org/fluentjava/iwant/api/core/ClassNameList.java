package org.fluentjava.iwant.api.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.StringFilter;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.entry.Iwant;

public class ClassNameList extends TargetBase {

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

		Iwant.textFileEnsuredToHaveContent(ctx.cached(this), out.toString());
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
		List<String> classNames = new ArrayList<>();
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
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("classes", classes).parameter("filter", filter)
				.nothingElse();
	}

	public Path classes() {
		return classes;
	}

	public StringFilter filter() {
		return filter;
	}

}
