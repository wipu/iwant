package org.fluentjava.iwant.api.javamodules;

import java.io.File;
import java.util.Arrays;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.entry.Iwant;

public class ScalaClassesTest extends IwantTestCase {

	private static final ScalaVersion SCALA = ScalaVersion.latestTested();

	@Override
	protected void moreSetUp() throws Exception {
		cacheProvidesRealDownloaded(SCALA.compilerJar().artifact().url());
		cacheProvidesRealDownloaded(SCALA.libraryJar().artifact().url());
		cacheProvidesRealDownloaded(SCALA.reflectJar().artifact().url());
		cacheProvidesRealDownloaded(
				TestedIwantDependencies.antJar().artifact().url());
		cacheProvidesRealDownloaded(
				TestedIwantDependencies.antLauncherJar().artifact().url());
	}

	public void testScalaAndJavaCompileAndRunWhenThereIsACrossDependencyBetweenTheLanguages()
			throws Exception {
		wsRootHasDirectory("depsrc/deppak");
		StringBuilder dep = new StringBuilder();
		dep.append("package deppak;\n");
		dep.append("public class Dependency {\n");
		dep.append("  public static String stringFromDependency() {\n");
		dep.append("    return \"string from dependency\";\n");
		dep.append("  \n}");
		dep.append("}\n");
		wsRootHasFile("depsrc/deppak/Dependency.java", dep.toString());
		JavaClasses depClasses = JavaClasses.with().name("dep-classes")
				.srcDirs(Source.underWsroot("depsrc")).end();
		depClasses.path(ctx);

		wsRootHasDirectory("src/main/java/pak");
		StringBuilder javaRoot = new StringBuilder();
		javaRoot.append("package pak;\n");
		javaRoot.append("public class JavaRoot {\n");
		javaRoot.append("  public static void main(String[] args) {\n");
		javaRoot.append(
				"    System.out.println(new ScalaClass().stringFromScala());\n");
		javaRoot.append("  \n}");
		javaRoot.append("}\n");
		wsRootHasFile("src/main/java/pak/JavaRoot.java", javaRoot.toString());

		StringBuilder javaHello = new StringBuilder();
		javaHello.append("package pak;\n");
		javaHello.append("public class JavaHello {\n");
		javaHello.append("  public static String stringFromJava() {\n");
		javaHello.append("    return \"string from java\";\n");
		javaHello.append("  \n}");
		javaHello.append("}\n");
		wsRootHasFile("src/main/java/pak/JavaHello.java", javaHello.toString());

		wsRootHasDirectory("src/main/scala/pak");
		StringBuilder scala = new StringBuilder();
		scala.append("package pak {\n");
		scala.append("  class ScalaClass {\n");
		scala.append("    def stringFromScala() : String = {\n");
		scala.append("      \"string from scala using \""
				+ " + pak.JavaHello.stringFromJava() + \" and \""
				+ " + deppak.Dependency.stringFromDependency();\n");
		scala.append("    \n}");
		scala.append("  }\n");
		scala.append("}\n");
		wsRootHasFile("src/main/scala/pak/ScalaClass.scala", scala.toString());

		// scala src compiles:
		ScalaClasses scalaClasses = ScalaClasses.with().name("scala-classes")
				.scala(SCALA)
				.srcDirs(Source.underWsroot("src/main/java"),
						Source.underWsroot("src/main/scala"))
				.classLocations(depClasses).end();
		scalaClasses.path(ctx);
		assertTrue(new File(cached, "scala-classes").exists());

		// java src compiles:
		JavaClasses javaClasses = JavaClasses.with().name("java-classes")
				.srcDirs(Source.underWsroot("src/main/java"))
				.classLocations(scalaClasses).end();
		javaClasses.path(ctx);

		// they run together:
		Iwant.runJavaMain(false, true, "pak.JavaRoot",
				Arrays.asList(ctx.cached(scalaClasses), ctx.cached(javaClasses),
						ctx.cached(depClasses),
						ctx.cached(SCALA.libraryJar())));

		assertEquals(
				"string from scala using string from java and string from dependency\n",
				out());
	}

	public void testContentDescriptorAndIngredients() {
		Path dep = Source.underWsroot("dep");
		ScalaClasses scalaClasses = ScalaClasses.with().name("scala-classes")
				.scala(SCALA)
				.srcDirs(Source.underWsroot("src/main/java"),
						Source.underWsroot("src/main/scala"))
				.classLocations(dep).end();

		assertEquals("org.fluentjava.iwant.api.javamodules.ScalaClasses\n"
				+ "i:srcDirs:\n" + "  src/main/java\n" + "  src/main/scala\n"
				+ "i:classLocations:\n" + "  dep\n" + "i:scala-compiler:\n"
				+ "  scala-compiler-2.12.13.jar\n" + "i:scala-library:\n"
				+ "  scala-library-2.12.13.jar\n" + "i:scala-reflect:\n"
				+ "  scala-reflect-2.12.13.jar\n" + "i:antJar:\n"
				+ "  ant-1.10.9.jar\n" + "i:antLauncherJar:\n"
				+ "  ant-launcher-1.10.9.jar\n" + "",
				scalaClasses.contentDescriptor());

		assertEquals(
				"[src/main/java, src/main/scala, dep, scala-compiler-2.12.13.jar,"
						+ " scala-library-2.12.13.jar, scala-reflect-2.12.13.jar,"
						+ " ant-1.10.9.jar, ant-launcher-1.10.9.jar]",
				scalaClasses.ingredients().toString());
	}

}
