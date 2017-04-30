package net.sf.iwant.plugin.jacoco;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import net.sf.iwant.api.antrunner.AntRunner;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.api.target.TargetBase;

public class JacocoInstrumentation extends TargetBase {

	private final Path classes;
	private final JacocoDistribution jacoco;
	private final List<Path> antJars;
	private final List<Path> deps;

	public JacocoInstrumentation(Path classes, JacocoDistribution jacoco,
			List<Path> antJars, List<Path> deps) {
		super(classes + ".jacoco-instr");
		this.classes = classes;
		this.jacoco = jacoco;
		this.antJars = antJars;
		this.deps = deps;
	}

	public static JacocoAndAntPlease of(Path classes) {
		return new Builder(classes);
	}

	public interface JacocoAndAntPlease {
		DepsPlease using(JacocoDistribution jacoco, Path... antJars);

		DepsPlease using(JacocoDistribution jacoco,
				Collection<? extends Path> antJars);
	}

	public interface DepsPlease {
		JacocoInstrumentation with(Path... deps);

		JacocoInstrumentation with(Collection<? extends Path> deps);
	}

	private static class Builder implements JacocoAndAntPlease, DepsPlease {

		private final Path classes;
		private JacocoDistribution jacoco;
		private final List<Path> antJars = new ArrayList<>();

		public Builder(Path classes) {
			this.classes = classes;
		}

		@Override
		public DepsPlease using(JacocoDistribution jacoco, Path... antJars) {
			return using(jacoco, Arrays.asList(antJars));
		}

		@Override
		public DepsPlease using(JacocoDistribution jacoco,
				Collection<? extends Path> antJars) {
			this.jacoco = jacoco;
			this.antJars.addAll(antJars);
			return this;
		}

		@Override
		public JacocoInstrumentation with(Path... deps) {
			return with(Arrays.asList(deps));
		}

		@Override
		public JacocoInstrumentation with(Collection<? extends Path> deps) {
			return new JacocoInstrumentation(classes, jacoco, antJars,
					new ArrayList<>(deps));
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("jacoco", jacoco).ingredients("deps", deps)
				.ingredients("antJars", antJars).ingredients("classes", classes)
				.nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		ctx.cached(this).mkdirs();
		File tmp = ctx.freshTemporaryDirectory();

		File antScript = new File(tmp, name() + ".xml");
		FileUtils.writeStringToFile(antScript, antScript(ctx), "UTF-8");

		List<File> cachedAntJars = new ArrayList<>();
		for (Path antJar : antJars) {
			cachedAntJars.add(ctx.cached(antJar));
		}
		AntRunner.runAnt(cachedAntJars, antScript);
	}

	private String antScript(TargetEvaluationContext ctx) {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		b.append("\n");
		b.append("<project name=\"" + name() + "\" default=\"" + name()
				+ "\" xmlns:jacoco=\"antlib:org.jacoco.ant\" basedir=\".\">\n");
		b.append("\n");
		b.append(
				"      <taskdef uri=\"antlib:org.jacoco.ant\" resource=\"org/jacoco/ant/antlib.xml\">\n");
		for (Path dep : deps) {
			b.append("              <classpath location=\"" + ctx.cached(dep)
					+ "\" />\n");
		}
		b.append("              <classpath location=\""
				+ jacoco.orgJacocoAntJar(ctx) + "\" />\n");
		b.append("              <classpath location=\""
				+ jacoco.orgJacocoCoreJar(ctx) + "\" />\n");
		b.append("              <classpath location=\""
				+ jacoco.orgJacocoReportJar(ctx) + "\" />\n");
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
