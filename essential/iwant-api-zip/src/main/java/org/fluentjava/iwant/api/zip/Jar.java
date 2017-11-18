package org.fluentjava.iwant.api.zip;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;

public class Jar extends TargetBase {

	private final List<Path> classDirs;

	public Jar(String name, List<Path> classDirs) {
		super(name);
		this.classDirs = classDirs;
	}

	public static JarSpex with() {
		return new JarSpex();
	}

	public static class JarSpex {

		private String name;
		private final List<Path> classDirs = new ArrayList<>();

		public JarSpex name(String name) {
			this.name = name;
			return this;
		}

		public JarSpex classes(Path classes) {
			this.classDirs.add(classes);
			return this;
		}

		public Jar end() {
			return new Jar(name, classDirs);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("classDirs", classDirs).nothingElse();
	}

	public List<Path> classDirs() {
		return Collections.unmodifiableList(classDirs);
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		Project project = new Project();
		org.apache.tools.ant.taskdefs.Jar jar = new org.apache.tools.ant.taskdefs.Jar();
		jar.setProject(project);

		jar.setDestFile(ctx.cached(this));
		for (Path classDir : classDirs) {
			FileSet fileSet = new FileSet();
			fileSet.setDir(ctx.cached(classDir));
			jar.addFileset(fileSet);
		}

		jar.execute();
	}

}
