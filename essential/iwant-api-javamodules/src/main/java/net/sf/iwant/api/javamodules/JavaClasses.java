package net.sf.iwant.api.javamodules;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.core.TargetBase;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;

public class JavaClasses extends TargetBase {

	private final Collection<? extends Path> srcDirs;
	private final Collection<? extends Path> resourceDirs;
	private final Collection<? extends Path> classLocations;
	private final boolean debug;
	private final JavaCompliance sourceVersion;
	private final Charset encoding;
	private final List<String> rawArgs;

	private JavaClasses(String name, Collection<? extends Path> srcDirs,
			Collection<? extends Path> resourceDirs, List<Path> classLocations,
			boolean debug, JavaCompliance sourceVersion, Charset encoding,
			List<String> rawArgs) {
		super(name);
		this.srcDirs = srcDirs;
		this.resourceDirs = resourceDirs;
		this.classLocations = classLocations;
		this.debug = debug;
		this.sourceVersion = sourceVersion;
		this.encoding = encoding;
		this.rawArgs = rawArgs;
	}

	public static JavaClassesSpex with() {
		return new JavaClassesSpex();
	}

	public static class JavaClassesSpex {

		private String name;
		private final List<Path> srcDirs = new ArrayList<>();
		private final List<Path> resourceDirs = new ArrayList<>();
		private final List<Path> classLocations = new ArrayList<>();
		private boolean debug;
		private Charset encoding;
		private JavaCompliance sourceVersion;
		private final List<String> rawArgs = new ArrayList<>();

		public JavaClassesSpex name(String name) {
			this.name = name;
			return this;
		}

		public JavaClassesSpex srcDirs(Path... srcDirs) {
			return srcDirs(Arrays.asList(srcDirs));
		}

		public JavaClassesSpex srcDirs(Collection<? extends Path> srcDirs) {
			this.srcDirs.addAll(srcDirs);
			return this;
		}

		public JavaClassesSpex noSrcDirs() {
			this.srcDirs.clear();
			return this;
		}

		public JavaClassesSpex resourceDirs(Path... resourceDirs) {
			return resourceDirs(Arrays.asList(resourceDirs));
		}

		public JavaClassesSpex resourceDirs(
				Collection<? extends Path> resourceDirs) {
			this.resourceDirs.addAll(resourceDirs);
			return this;
		}

		public JavaClassesSpex noResourceDirs() {
			this.resourceDirs.clear();
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

		public JavaClassesSpex debug(boolean debug) {
			this.debug = debug;
			return this;
		}

		public JavaClassesSpex sourceVersion(JavaCompliance sourceVersion) {
			this.sourceVersion = sourceVersion;
			return this;
		}

		public JavaClassesSpex encoding(Charset encoding) {
			this.encoding = encoding;
			return this;
		}

		public JavaClassesSpex rawArgs(String... rawArgs) {
			return rawArgs(Arrays.asList(rawArgs));
		}

		public JavaClassesSpex rawArgs(Collection<? extends String> rawArgs) {
			this.rawArgs.addAll(rawArgs);
			return this;
		}

		public JavaClasses end() {
			return new JavaClasses(name, srcDirs, resourceDirs, classLocations,
					debug, sourceVersion, encoding, rawArgs);
		}

	}

	public Collection<? extends Path> classLocations() {
		return classLocations;
	}

	public Collection<? extends Path> srcDirs() {
		return srcDirs;
	}

	public Charset encoding() {
		return encoding;
	}

	public boolean debug() {
		return debug;
	}

	public Collection<? extends Path> resourceDirs() {
		return resourceDirs;
	}

	public JavaCompliance sourceCompliance() {
		return sourceVersion;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	public List<String> javacOptions() {
		List<String> javacOptions = new ArrayList<>();
		javacOptions.addAll(Iwant.recommendedJavacWarningOptions());
		if (sourceVersion != null) {
			javacOptions.add("-source");
			javacOptions.add(sourceVersion.prettyName());
		}
		if (debug) {
			javacOptions.add("-g");
		}
		for (String rawArg : rawArgs) {
			javacOptions.add(rawArg);
		}
		return javacOptions;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		List<File> javaFiles = new ArrayList<>();
		for (Path srcDir : srcDirs) {
			javaFiles.addAll(javaFilesUnder(ctx.cached(srcDir)));
		}
		if (javaFiles.isEmpty() && resourceDirs().isEmpty()) {
			ctx.iwant().debugLog(getClass().getSimpleName(),
					"No java files to compile or resources to copy.");
			dest.mkdirs();
			return;
		}
		if (!javaFiles.isEmpty()) {
			List<File> classLocationDirs = new ArrayList<>();
			for (Path classLocation : classLocations) {
				File classLocationDir = ctx.cached(classLocation);
				classLocationDirs.add(classLocationDir);
			}
			ctx.iwant().compiledClasses(dest, javaFiles, classLocationDirs,
					javacOptions(), encoding);
		} else {
			// create dest for resource copying
			dest.mkdirs();
		}
		for (Path res : resourceDirs()) {
			File cachedRes = ctx.cached(res);
			ctx.iwant().copyMissingFiles(cachedRes, dest);
		}
	}

	private static List<File> javaFilesUnder(File dir) {
		if (!dir.exists()) {
			throw new IllegalArgumentException(
					"Source directory does not exist: " + dir);
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(
					"Source is not a directory: " + dir);
		}
		List<File> srcFiles = new ArrayList<>();
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
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("srcDirs", srcDirs)
				.ingredients("resourceDirs", resourceDirs)
				.ingredients("classLocations", classLocations)
				.parameter("javacOptions", javacOptions())
				.parameter("encoding", encoding()).nothingElse();
	}

}
