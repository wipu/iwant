package net.sf.iwant.plugin.ant;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

import org.apache.tools.ant.Project;

public class Jar extends Target {

	private final Path classes;
	private final String classesSubDirectory;

	public Jar(String name, Path classes, String classesSubDirectory) {
		super(name);
		this.classes = classes;
		this.classesSubDirectory = classesSubDirectory;
	}

	public static JarSpex with() {
		return new JarSpex();
	}

	public static class JarSpex {

		private String name;
		private Path classes;
		private String classesSubDirectory;

		public JarSpex name(String name) {
			this.name = name;
			return this;
		}

		public JarSpex classes(Path classes) {
			this.classes = classes;
			return this;
		}

		public JarSpex classesSubDirectory(String classesSubDirectory) {
			this.classesSubDirectory = classesSubDirectory;
			return this;
		}

		public Jar end() {
			return new Jar(name, classes, classesSubDirectory);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		return Arrays.asList(classes);
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName() + ": {\n");
		if (classesSubDirectory != null) {
			b.append("  classes-sub-directory:").append(classesSubDirectory)
					.append("\n");
		}
		b.append("  ingredients: {\n");
		for (Path ingredient : ingredients()) {
			b.append("    ").append(ingredient).append("\n");
		}
		b.append("  }\n");
		b.append("}\n");
		return b.toString();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		Project project = new Project();
		org.apache.tools.ant.taskdefs.Jar jar = new org.apache.tools.ant.taskdefs.Jar();
		jar.setProject(project);

		File cachedClasses = ctx.cached(classes);
		File baseDir = classesSubDirectory == null ? cachedClasses : new File(
				cachedClasses, classesSubDirectory);

		jar.setBasedir(baseDir);
		jar.setDestFile(ctx.cached(this));

		jar.execute();
	}

}
