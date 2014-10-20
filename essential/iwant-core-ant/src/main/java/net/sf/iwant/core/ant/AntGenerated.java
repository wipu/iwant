package net.sf.iwant.core.ant;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.ExitCalledException;

public class AntGenerated extends Target {

	private final List<Path> antJars;
	private final Path script;

	private AntGenerated(String name, List<Path> antJars, Path script) {
		super(name);
		this.antJars = antJars;
		this.script = script;
	}

	public static AntGeneratedSpex with() {
		return new AntGeneratedSpex();
	}

	public static class AntGeneratedSpex {

		private String name;
		private final List<Path> antJars = new ArrayList<Path>();
		private Path script;

		public AntGeneratedSpex name(String name) {
			this.name = name;
			return this;
		}

		public AntGeneratedSpex antJars(Path... antJar) {
			this.antJars.addAll(Arrays.asList(antJar));
			return this;
		}

		public AntGeneratedSpex script(Path script) {
			this.script = script;
			return this;
		}

		public AntGenerated end() {
			return new AntGenerated(name, antJars, script);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		// ant.sh calls this but it is not probably good for embedding:
		// String className = "org.apache.tools.ant.launch.Launcher";

		List<File> cachedJars = new ArrayList<File>();
		for (Path jar : antJars) {
			cachedJars.add(ctx.cached(jar));
		}
		File cachedScript = ctx.cached(script);

		runAnt(cachedJars, cachedScript, "-Diwant-outfile=" + ctx.cached(this));
	}

	public static void runAnt(List<File> antJars, File cachedScript,
			String... antArgs) throws Exception {
		final String className = "org.apache.tools.ant.Main";
		List<String> allArgs = new ArrayList<String>();
		allArgs.add("-f");
		allArgs.add(cachedScript.getAbsolutePath());
		allArgs.addAll(Arrays.asList(antArgs));
		try {
			Iwant.runJavaMain(true, false, className, antJars,
					allArgs.toArray(new String[0]));
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof ExitCalledException) {
				ExitCalledException ece = (ExitCalledException) e.getCause();
				if (ece.status() != 0) {
					throw ece;
				} else {
					// ant just feels like trying to kill our JVM,
					// it was still a success
				}
			} else {
				throw asRuntimeException(e.getCause());
			}
		}
	}

	private static RuntimeException asRuntimeException(Throwable e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

	@Override
	public List<Path> ingredients() {
		List<Path> retval = new ArrayList<Path>();
		retval.addAll(antJars);
		retval.add(script);
		return retval;
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");
		for (Path antJar : antJars) {
			b.append("  ant-jar:").append(antJar).append("\n");
		}
		b.append("  script:").append(script).append("\n");
		b.append("}\n");
		return b.toString();
	}

}
