package net.sf.iwant.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.entry.Iwant;

public class EmmaInstrumentation extends Target {

	private final Path emma;
	private final JavaClassesAndSources classesAndSources;
	private final Path filter;

	public EmmaInstrumentation(JavaClassesAndSources classesAndSources,
			Path emma, Path filter) {
		super(classesAndSources.name() + ".emma-instr");
		this.classesAndSources = classesAndSources;
		this.emma = emma;
		this.filter = filter;
	}

	public static EmmaInstrumentationUsing of(
			JavaClassesAndSources classesAndSources) {
		return new EmmaInstrumentationUsing(classesAndSources);
	}

	public static EmmaInstrumentationUsing of(JavaModule mod) {
		if (!(mod instanceof JavaSrcModule)) {
			return new EmmaInstrumentationUsing(null);
		}
		JavaSrcModule srcMod = (JavaSrcModule) mod;
		if (srcMod.mainJavasAsPaths().isEmpty()) {
			return new EmmaInstrumentationUsing(null);
		}
		JavaClassesAndSources classesAndSources = new JavaClassesAndSources(
				srcMod.mainArtifact(), srcMod.mainJavasAsPaths());
		return new EmmaInstrumentationUsing(classesAndSources);
	}

	public static class EmmaInstrumentationUsing {

		private final JavaClassesAndSources classesAndSources;
		private Path filter;

		public EmmaInstrumentationUsing(JavaClassesAndSources classesAndSources) {
			this.classesAndSources = classesAndSources;
		}

		public EmmaInstrumentationUsing filter(Path filter) {
			this.filter = filter;
			return this;
		}

		public EmmaInstrumentation using(Path emma) {
			if (classesAndSources == null) {
				return null;
			}
			return new EmmaInstrumentation(classesAndSources, emma, filter);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dir = ctx.cached(this);
		File em = metadataFile(ctx);
		File ec = new File(dir, "please-override-when-running-tests.ec");
		File instrClasses = new File(dir, "instr-classes");
		String emmaLogLevel = "warning";
		File instrProps = Iwant.newTextFile(new File(dir,
				"emma-instr.properties"), "metadata.out.file="
				+ wintoySafeCanonicalPath(em) + "\nverbosity.level="
				+ emmaLogLevel + "\n" + "coverage.out.file="
				+ wintoySafeCanonicalPath(ec) + "\n");

		File cachedClasses = ctx.cached(classesAndSources.classes());
		File cachedEmma = ctx.cached(emma);

		List<String> emmaArgs = new ArrayList<String>();
		emmaArgs.add("instr");
		emmaArgs.add("-d");
		emmaArgs.add(instrClasses.getCanonicalPath());
		emmaArgs.add("-properties");
		emmaArgs.add(instrProps.getCanonicalPath());
		emmaArgs.add("-ip");
		emmaArgs.add(cachedClasses.getCanonicalPath());
		if (filter != null) {
			emmaArgs.add("-filter");
			emmaArgs.add("@" + ctx.cached(filter).getCanonicalPath());
		}
		runEmma(cachedEmma, emmaArgs.toArray(new String[0]));

		int nOfFileCopied = FileUtil.copyMissingFiles(cachedClasses,
				instrClasses);
		System.err.println("Copied " + nOfFileCopied
				+ " files emma excluded from instrumented classes.");
	}

	private static String wintoySafeCanonicalPath(File file) throws IOException {
		return BackslashFixer.wintoySafeCanonicalPath(file);
	}

	public File metadataFile(TargetEvaluationContext ctx) {
		return new File(ctx.cached(this), "emma.em");
	}

	public JavaClassesAndSources classesAndSources() {
		return classesAndSources;
	}

	static void runEmma(File emmaJar, String... emmaArgs) throws Exception {
		List<File> classLocations = new ArrayList<File>();
		classLocations.add(emmaJar);
		Iwant.runJavaMain(true, true, "emma", classLocations, emmaArgs);
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.add(emma);
		ingredients.add(classesAndSources.classes());
		if (filter != null) {
			ingredients.add(filter);
		}
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

}
