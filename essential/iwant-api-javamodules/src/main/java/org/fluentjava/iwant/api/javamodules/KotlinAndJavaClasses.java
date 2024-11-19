package org.fluentjava.iwant.api.javamodules;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.fluentjava.iwant.api.core.ScriptGenerated;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.coreservices.FileUtil;
import org.fluentjava.iwant.entry.Iwant;

public class KotlinAndJavaClasses extends TargetBase {

	private final Path kotlinAntJar;
	private final List<Path> srcDirs;
	private final List<Path> resourceDirs;
	private final List<Path> classLocations;

	public KotlinAndJavaClasses(String name, KotlinVersion kotlin,
			Collection<? extends Path> srcDirs,
			Collection<? extends Path> resourceDirs,
			Collection<? extends Path> classLocations) {
		super(name);
		this.kotlinAntJar = kotlin.kotlinAntJar();
		this.srcDirs = new ArrayList<>(srcDirs);
		this.resourceDirs = new ArrayList<>(resourceDirs);
		this.classLocations = new ArrayList<>(classLocations);
	}

	public List<Path> srcDirs() {
		return srcDirs;
	}

	public List<Path> resourceDirs() {
		return resourceDirs;
	}

	public List<Path> classLocations() {
		return classLocations;
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("srcDirs", srcDirs)
				.ingredients("resourceDirs", resourceDirs)
				.ingredients("classLocations", classLocations)
				.ingredients("kotlin-ant.jar", kotlinAntJar).nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File tmp = ctx.freshTemporaryDirectory();
		File buildXml = new File(tmp, "build.xml");
		FileUtil.textFileEnsuredToHaveContent(buildXml, buildXml(ctx));
		File buildSh = new File(tmp, "build.sh");
		FileUtil.textFileEnsuredToHaveContent(buildSh, buildSh());
		buildSh.setExecutable(true);

		ScriptGenerated.execute(tmp, Arrays.asList("./build.sh"));
	}

	/**
	 * TODO find out why ant javac deduces JAVA_HOME incorrectly from
	 * JAVA_HOME/jre so this is needed
	 */
	private static File toolsJar() {
		String javaHome = System.getenv("JAVA_HOME");
		if (javaHome == null) {
			throw new Iwant.IwantException("JAVA_HOME not set.");
		}
		File toolsJar = new File(javaHome + "/lib/tools.jar");
		return toolsJar;
	}

	private static String buildSh() {
		StringBuilder b = new StringBuilder();
		b.append("#!/bin/bash\n");
		b.append("set -euo pipefail\n");
		b.append("ant\n");
		return b.toString();
	}

	// TODO configurable java compliance, encoding, debug
	private String buildXml(TargetEvaluationContext ctx) {
		StringBuilder b = new StringBuilder();
		b.append("<project name=\"" + name()
				+ "\" default=\"kotlin-and-java-classes\">\n");
		b.append(
				"  <typedef resource=\"org/jetbrains/kotlin/ant/antlib.xml\" classpath=\""
						+ ctx.cached(kotlinAntJar) + ":" + toolsJar()
						+ "\"/>\n");
		b.append("\n");
		b.append("  <target name=\"kotlin-and-java-classes\">\n");

		b.append("    <mkdir dir=\"" + ctx.cached(this) + "\"/>\n");
		b.append("    <javac destdir=\"" + ctx.cached(this)
				+ "\" includeAntRuntime=\"true\" includeJavaRuntime=\"true\" bootclasspath=\""
				+ toolsJar()
				+ "\" release=\"17\" encoding=\"UTF-8\" debug=\"true\">\n");

		b.append("      <withKotlin/>\n");
		b.append("      <compilerclasspath>\n");
		b.append("        <pathelement location=\"" + toolsJar() + "\"/>\n");
		b.append("      </compilerclasspath>\n");
		b.append("      <src>\n");
		for (Path src : srcDirs) {
			b.append("        <pathelement location=\"" + ctx.cached(src)
					+ "\"/>\n");
		}
		b.append("      </src>\n");
		b.append("      <classpath>\n");
		for (Path dep : classLocations) {
			b.append("        <pathelement location=\"" + ctx.cached(dep)
					+ "\"/>\n");
		}
		b.append("      </classpath>\n");
		b.append("    </javac>\n");
		b.append("  </target>\n");
		b.append("</project>");
		return b.toString();
	}

}
