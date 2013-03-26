package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.entry.Iwant;

public class JavaClasses extends Target {

	private final List<Path> ingredients;
	private final Collection<? extends Path> srcDirs;
	private final Collection<? extends Path> classLocations;

	private JavaClasses(String name, Collection<? extends Path> srcDirs,
			Collection<? extends Path> classLocations) {
		super(name);
		this.srcDirs = srcDirs;
		this.classLocations = classLocations;
		this.ingredients = new ArrayList<Path>();
		this.ingredients.addAll(srcDirs);
		this.ingredients.addAll(classLocations);
	}

	public static JavaClassesSpex with() {
		return new JavaClassesSpex();
	}

	public static class JavaClassesSpex {

		private String name;
		private final List<Path> srcDirs = new ArrayList<Path>();
		private final List<Path> classLocations = new ArrayList<Path>();

		public JavaClassesSpex name(String name) {
			this.name = name;
			return this;
		}

		public JavaClassesSpex srcDirs(Path... srcDirs) {
			this.srcDirs.addAll(Arrays.asList(srcDirs));
			return this;
		}

		public JavaClassesSpex noSrcDirs() {
			this.srcDirs.clear();
			return this;
		}

		public JavaClassesSpex classLocations(Path... classLocations) {
			return classLocations(Arrays.asList(classLocations));
		}

		public JavaClassesSpex classLocations(
				Collection<? extends Path> classLocations) {
			this.classLocations.addAll(classLocations);
			return this;
		}

		public JavaClasses end() {
			return new JavaClasses(name, srcDirs, classLocations);
		}

	}

	public Collection<? extends Path> classLocations() {
		return classLocations;
	}

	public Collection<? extends Path> srcDirs() {
		return srcDirs;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		List<File> javaFiles = new ArrayList<File>();
		for (Path srcDir : srcDirs) {
			javaFiles.addAll(javaFilesUnder(ctx.cached(srcDir)));
		}
		if (javaFiles.isEmpty()) {
			Iwant.debugLog(getClass().getSimpleName(),
					"No java files to compile.");
			dest.mkdirs();
			return;
		}
		List<File> classLocationDirs = new ArrayList<File>();
		for (Path classLocation : classLocations) {
			File classLocationDir = ctx.cached(classLocation);
			classLocationDirs.add(classLocationDir);
		}
		ctx.iwant().compiledClasses(dest, javaFiles, classLocationDirs);
	}

	private static List<File> javaFilesUnder(File dir) {
		if (!dir.exists()) {
			throw new IllegalArgumentException(
					"Source directory does not exist: " + dir);
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Source is not a directory: "
					+ dir);
		}
		List<File> srcFiles = new ArrayList<File>();
		File[] files = dir.listFiles();
		Arrays.sort(files);
		for (File file : files) {
			if (file.isDirectory()) {
				srcFiles.addAll(javaFilesUnder(file));
			} else {
				if (file.getAbsolutePath().endsWith(".java")) {
					srcFiles.add(file);
				}
			}
		}
		return srcFiles;
	}

	@Override
	public List<Path> ingredients() {
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");
		for (Path srcDir : srcDirs) {
			b.append("  src:").append(srcDir).append("\n");
		}
		for (Path classLocation : classLocations) {
			b.append("  classes:").append(classLocation).append("\n");
		}
		b.append("}");
		return b.toString();
	}

}
