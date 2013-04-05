package net.sf.iwant.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry3.FileUtil;

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
				"emma-instr.properties"),
				"metadata.out.file=" + em.getCanonicalPath()
						+ "\nverbosity.level=" + emmaLogLevel + "\n"
						+ "coverage.out.file=" + ec.getCanonicalPath() + "\n");

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

		int nOfFileCopied = copyMissingFiles(cachedClasses, instrClasses);
		System.err.println("Copied " + nOfFileCopied
				+ " files emma excluded from instrumented classes.");
	}

	private int copyMissingFiles(File from, File to) throws IOException {
		int count = 0;
		for (File child : from.listFiles()) {
			File toChild = new File(to, child.getName());
			if (!toChild.exists()) {
				count += copyRecursively(child, toChild);
				continue;
			}
			if (child.isDirectory()) {
				count += copyMissingFiles(child, toChild);
			}
		}
		return count;
	}

	private int copyRecursively(File from, File to) throws IOException {
		if (from.isDirectory()) {
			int count = 0;
			to.mkdir();
			for (File child : from.listFiles()) {
				File toChild = new File(to, child.getName());
				count += copyRecursively(child, toChild);
			}
			return count;
		} else {
			FileUtil.copyFile(from, to);
			return 1;
		}
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
