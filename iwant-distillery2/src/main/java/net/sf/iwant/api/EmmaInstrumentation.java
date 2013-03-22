package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.entry.Iwant;

public class EmmaInstrumentation extends Target {

	private final Path emma;
	private final JavaClassesAndSources classesAndSources;

	public EmmaInstrumentation(JavaClassesAndSources classesAndSources,
			Path emma) {
		super(classesAndSources.name() + ".emma-instr");
		this.classesAndSources = classesAndSources;
		this.emma = emma;
	}

	public static EmmaInstrumentationUsing of(
			JavaClassesAndSources classesAndSources) {
		return new EmmaInstrumentationUsing(classesAndSources);
	}

	public static class EmmaInstrumentationUsing {

		private final JavaClassesAndSources classesAndSources;

		public EmmaInstrumentationUsing(JavaClassesAndSources classesAndSources) {
			this.classesAndSources = classesAndSources;
		}

		public EmmaInstrumentation using(Path emma) {
			return new EmmaInstrumentation(classesAndSources, emma);
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

		runEmma(cachedEmma, "instr", "-d", instrClasses.getCanonicalPath(),
				"-properties", instrProps.getCanonicalPath(), "-ip",
				cachedClasses.getCanonicalPath());
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
		return Arrays.asList(emma, classesAndSources.classes());
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

}
