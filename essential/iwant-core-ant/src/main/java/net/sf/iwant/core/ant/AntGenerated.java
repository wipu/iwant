package net.sf.iwant.core.ant;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.TargetBase;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.ExitCalledException;

public class AntGenerated extends TargetBase {

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
		private final List<Path> antJars = new ArrayList<>();
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
	public void path(TargetEvaluationContext ctx) throws Exception {
		// ant.sh calls this but it is not probably good for embedding:
		// String className = "org.apache.tools.ant.launch.Launcher";

		List<File> cachedJars = new ArrayList<>();
		for (Path jar : antJars) {
			cachedJars.add(ctx.cached(jar));
		}
		File cachedScript = ctx.cached(script);

		runAnt(cachedJars, cachedScript, "-Diwant-outfile=" + ctx.cached(this));
	}

	public static void runAnt(List<File> antJars, File cachedScript,
			String... antArgs) throws Exception {
		final String className = "org.apache.tools.ant.Main";
		List<String> allArgs = new ArrayList<>();
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
	protected IngredientsAndParametersDefined ingredientsAndAttributes(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("ant-jars", antJars)
				.ingredients("script", script).nothingElse();
	}

}
