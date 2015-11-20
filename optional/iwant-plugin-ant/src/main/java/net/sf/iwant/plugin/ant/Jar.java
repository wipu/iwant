package net.sf.iwant.plugin.ant;

import java.io.File;
import java.io.InputStream;

import org.apache.tools.ant.Project;

import net.sf.iwant.api.core.TargetBase;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;

public class Jar extends TargetBase {

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
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("classes", classes)
				.parameter("classesSubDirectory", classesSubDirectory)
				.nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		Project project = new Project();
		org.apache.tools.ant.taskdefs.Jar jar = new org.apache.tools.ant.taskdefs.Jar();
		jar.setProject(project);

		File cachedClasses = ctx.cached(classes);
		File baseDir = classesSubDirectory == null ? cachedClasses
				: new File(cachedClasses, classesSubDirectory);

		jar.setBasedir(baseDir);
		jar.setDestFile(ctx.cached(this));

		jar.execute();
	}

}
