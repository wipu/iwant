package org.fluentjava.iwant.plugin.jacoco;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.fluentjava.iwant.api.antrunner.AntRunner;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.entry.Iwant;

public class JacocoInstrumentation extends TargetBase {

	private final Path classes;
	private final JacocoDistribution jacoco;
	private final List<Path> antJars;

	public JacocoInstrumentation(Path classes, JacocoDistribution jacoco,
			List<Path> antJars) {
		super(classes + ".jacoco-instr");
		this.classes = classes;
		this.jacoco = jacoco;
		this.antJars = antJars;
	}

	public static JacocoAndAntPlease of(Path classes) {
		return new Builder(classes);
	}

	public interface JacocoAndAntPlease {
		JacocoInstrumentation using(JacocoDistribution jacoco, Path... antJars);

		JacocoInstrumentation using(JacocoDistribution jacoco,
				Collection<? extends Path> antJars);
	}

	private static class Builder implements JacocoAndAntPlease {

		private final Path classes;

		public Builder(Path classes) {
			this.classes = classes;
		}

		@Override
		public JacocoInstrumentation using(JacocoDistribution jacoco,
				Path... antJars) {
			return using(jacoco, Arrays.asList(antJars));
		}

		@Override
		public JacocoInstrumentation using(JacocoDistribution jacoco,
				Collection<? extends Path> antJars) {
			return new JacocoInstrumentation(classes, jacoco,
					new ArrayList<>(antJars));
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("jacoco", jacoco)
				.ingredients("antJars", antJars).ingredients("classes", classes)
				.nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		Iwant.mkdirs(ctx.cached(this));
		File tmp = ctx.freshTemporaryDirectory();

		File antScript = new File(tmp, name() + ".xml");
		FileUtils.writeStringToFile(antScript, antScript(ctx), "UTF-8");

		List<File> cachedAntJars = new ArrayList<>();
		for (Path antJar : antJars) {
			cachedAntJars.add(ctx.cached(antJar));
		}
		AntRunner.runAnt(cachedAntJars, antScript);
	}

	/**
	 * Adapted from
	 * https://www.jacoco.org/jacoco/trunk/doc/examples/build/build.xml
	 */
	private String antScript(TargetEvaluationContext ctx) {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		b.append("\n");
		b.append("<project name=\"" + name() + "\" default=\"" + name()
				+ "\" xmlns:jacoco=\"antlib:org.jacoco.ant\" basedir=\".\">\n");
		b.append("\n");
		b.append(
				"      <taskdef uri=\"antlib:org.jacoco.ant\" resource=\"org/jacoco/ant/antlib.xml\">\n");
		b.append("              <classpath location=\""
				+ jacoco.jacocoantJar(ctx) + "\" />\n");
		b.append("      </taskdef>\n");
		b.append("\n");
		b.append("      <target name=\"" + name() + "\">\n");
		b.append("              <jacoco:instrument destdir=\""
				+ ctx.cached(this) + "\">\n");
		b.append("                      <fileset dir=\"" + ctx.cached(classes)
				+ "\" includes=\"**/*\" />\n");
		b.append("              </jacoco:instrument>\n");
		b.append("      </target>\n");
		b.append("\n");
		b.append("</project>\n");
		return b.toString();
	}

}
