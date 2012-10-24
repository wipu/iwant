package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

		final String className = "org.apache.tools.ant.Main";
		List<File> cachedJars = new ArrayList<File>();
		for (Path jar : antJars) {
			cachedJars.add(ctx.cached(jar));
		}
		String cachedScript = ctx.cached(script).getAbsolutePath();
		try {
			Iwant.runJavaMain(true, false, className, cachedJars, "-f",
					cachedScript, "-Diwant-outfile=" + ctx.cached(this));
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof ExitCalledException) {
				ExitCalledException ece = (ExitCalledException) e.getCause();
				if (ece.status() != 0) {
					throw e;
				} else {
					// ant just feels like trying to kill our JVM,
					// it was still a success
				}
			} else {
				throw e;
			}
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
