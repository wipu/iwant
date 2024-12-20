package org.fluentjava.iwant.api.javamodules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.fluentjava.iwant.api.javamodules.JavaClasses.JavaClassesSpex;
import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.apimocks.TargetMock;
import org.fluentjava.iwant.coreservices.FileUtil;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.Test;

public class JavaClassesTest extends IwantTestCase {

	@Override
	protected boolean mustCaptureSystemOutAndErr() {
		return true;
	}

	@Test
	public void srcDirIsAnIgredient() {
		Path src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("classes").srcDirs(src)
				.classLocations().end();

		assertTrue(target.ingredients().contains(src));
	}

	@Test
	public void srcDirsAreIgredients() {
		Path src1 = Source.underWsroot("src1");
		Path src2 = Source.underWsroot("src1");
		Target target = JavaClasses.with().name("classes").srcDirs(src1, src2)
				.classLocations().end();

		assertTrue(target.ingredients().contains(src1));
		assertTrue(target.ingredients().contains(src2));
	}

	@Test
	public void resourceDirsAreIgredients() {
		Path res1 = Source.underWsroot("res1");
		Path res2 = Source.underWsroot("res1");
		Target target = JavaClasses.with().name("classes")
				.resourceDirs(res1, res2).classLocations().end();

		assertTrue(target.ingredients().contains(res1));
		assertTrue(target.ingredients().contains(res2));
	}

	@Test
	public void srcDirsCanBeEmptiedDuringSpecifyingJavaClasses() {
		Path src1 = Source.underWsroot("src1");
		Path src2 = Source.underWsroot("src1");
		JavaClassesSpex spex = JavaClasses.with().name("classes").srcDirs(src1)
				.classLocations();

		spex = spex.noSrcDirs().srcDirs(src2);

		JavaClasses target = spex.end();

		assertFalse(target.ingredients().contains(src1));
		assertTrue(target.ingredients().contains(src2));
	}

	@Test
	public void relevantSettingsAreMentionedInContentDescriptor() {
		assertEquals("org.fluentjava.iwant.api.javamodules.JavaClasses\n"
				+ "i:srcDirs:\n" + "  src\n" + "i:resourceDirs:\n"
				+ "i:classLocations:\n" + "p:javacOptions:\n" + "  -Xlint\n"
				+ "  -Xlint:-serial\n" + "p:encoding:\n" + "  UTF-8\n" + "",
				JavaClasses.with().name("classes")
						.srcDirs(Source.underWsroot("src")).classLocations()
						.debug(false).encoding(Charset.forName("UTF-8")).end()
						.contentDescriptor());
		assertEquals("org.fluentjava.iwant.api.javamodules.JavaClasses\n"
				+ "i:srcDirs:\n" + "  src2\n" + "  src3\n" + "i:resourceDirs:\n"
				+ "  res\n" + "i:classLocations:\n" + "p:javacOptions:\n"
				+ "  -Xlint\n" + "  -Xlint:-serial\n" + "  -g\n"
				+ "p:encoding:\n" + "  ISO-8859-1\n" + "",
				JavaClasses.with().name("classes2")
						.srcDirs(Source.underWsroot("src2"),
								Source.underWsroot("src3"))
						.resourceDirs(Source.underWsroot("res"))
						.classLocations().debug(true)
						.encoding(Charset.forName("ISO-8859-1")).end()
						.contentDescriptor());
	}

	@Test
	public void crapToPathFails() throws Exception {
		wsRootHasFile("src/Crap.java", "crap");
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("crap").srcDirs(src)
				.classLocations().end();

		try {
			target.path(ctx);
			fail();
		} catch (IwantException e) {
			assertEquals("Compilation failed.", e.getMessage());
		}
	}

	@Test
	public void sourceCompliance1_7WarnsAboutMissingBootclasspath()
			throws Exception {
		wsRootHasFile("src/Valid.java", "class Valid {}");
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("valid").srcDirs(src)
				.sourceVersion(JavaCompliance.JAVA_1_7).classLocations().end();

		target.path(ctx);

		assertTrue(new File(cached, "valid/Valid.class").exists());
		assertEquals(
				"warning: [options] bootstrap class path not set in conjunction with -source 7\n"
						+ "warning: [options] source value 7 is obsolete and will be removed in a future release\n"
						+ "warning: [options] To suppress warnings about obsolete options, use -Xlint:-options.\n"
						+ "3 warnings\n" + "",
				err());
	}

	/**
	 * Without this we get warning: [options] system modules path not set in
	 * conjunction with -source 11
	 */
	@Test
	public void javacOptionsSourceAndTargetAreReplacedWithReleaseFromJava11On() {
		Source src = Source.underWsroot("src");

		assertEquals("[-Xlint, -Xlint:-serial, -source, 1.8]",
				JavaClasses.with().name("classes").srcDirs(src)
						.sourceVersion(JavaCompliance.JAVA_1_8).classLocations()
						.end().javacOptions().toString());

		assertEquals("[-Xlint, -Xlint:-serial, --release, 11]",
				JavaClasses.with().name("classes").srcDirs(src)
						.sourceVersion(JavaCompliance.JAVA_11).classLocations()
						.end().javacOptions().toString());

		assertEquals("[-Xlint, -Xlint:-serial, --release, 17]",
				JavaClasses.with().name("classes").srcDirs(src)
						.sourceVersion(JavaCompliance.JAVA_17).classLocations()
						.end().javacOptions().toString());
	}

	@Test
	public void toPathCompilesFromMultiplePackages() throws Exception {
		wsRootHasFile("src/Caller.java",
				"class Caller {pak1.Callee1 callee1;pak2.Callee2 callee2;}");
		wsRootHasFile("src/pak1/Callee1.java",
				"package pak1;\npublic class Callee1 {}");
		wsRootHasFile("src/pak2/Callee2.java",
				"package pak2;\npublic class Callee2 {}");
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("multiple").srcDirs(src)
				.classLocations().end();

		target.path(ctx);

		assertTrue(new File(cached, "multiple/Caller.class").exists());
		assertTrue(new File(cached, "multiple/pak1/Callee1.class").exists());
		assertTrue(new File(cached, "multiple/pak2/Callee2.class").exists());
	}

	@Test
	public void toPathCompilesFromMultipleSrcDirs() throws Exception {
		File srcDir1 = new File(wsRoot, "src1");
		Iwant.textFileEnsuredToHaveContent(new File(srcDir1, "Caller.java"),
				"class Caller {pak1.Callee1 callee1;pak2.Callee2 callee2;}");

		File srcDir2 = new File(wsRoot, "src2");
		Iwant.textFileEnsuredToHaveContent(
				new File(srcDir2, "pak1/Callee1.java"),
				"package pak1;\npublic class Callee1 {}");
		File srcDir3 = new File(wsRoot, "src3");
		Iwant.textFileEnsuredToHaveContent(
				new File(srcDir3, "pak2/Callee2.java"),
				"package pak2;\npublic class Callee2 {}");

		Target target = JavaClasses
				.with().name("multiple").srcDirs(Source.underWsroot("src1"),
						Source.underWsroot("src2"), Source.underWsroot("src3"))
				.classLocations().end();

		target.path(ctx);

		assertTrue(new File(cached, "multiple/Caller.class").exists());
		assertTrue(new File(cached, "multiple/pak1/Callee1.class").exists());
		assertTrue(new File(cached, "multiple/pak2/Callee2.class").exists());
	}

	@Test
	public void classWithDepToClassesCompiles() throws Exception {
		Class<?> superClass = SuperClassForJavaClassesTestSubclass.class;
		File superClassFile = new File(getClass()
				.getResource(superClass.getSimpleName() + ".class").toURI());
		File srcDir = new File(wsRoot, "src");
		Iwant.textFileEnsuredToHaveContent(new File(srcDir, "Subclass.java"),
				"class Subclass extends " + superClass.getCanonicalName()
						+ "{}");
		Source src = Source.underWsroot("src");
		File superClassClasses = superClassFile.getParentFile().getParentFile()
				.getParentFile().getParentFile().getParentFile().getParentFile()
				.getAbsoluteFile();
		Target target = JavaClasses.with().name("valid").srcDirs(src)
				.classLocations(ExternalSource.at(superClassClasses)).end();

		target.path(ctx);

		assertTrue(new File(cached, "valid/Subclass.class").exists());
	}

	@Test
	public void dependenciesAreIgredients() {
		Target dep1 = new TargetMock("dep1");
		Target dep2 = new TargetMock("dep2");
		Target target = JavaClasses.with().name("valid")
				.srcDirs(Source.underWsroot("src")).classLocations(dep1, dep2)
				.end();

		assertTrue(target.ingredients().contains(dep1));
		assertTrue(target.ingredients().contains(dep2));
	}

	@Test
	public void dependenciesAreInContentDescriptor() {
		Target dep1 = new TargetMock("dep1");
		Target dep2 = new TargetMock("dep2");
		Target target = JavaClasses.with().name("valid")
				.srcDirs(Source.underWsroot("src")).classLocations(dep1, dep2)
				.end();

		assertEquals("org.fluentjava.iwant.api.javamodules.JavaClasses\n"
				+ "i:srcDirs:\n" + "  src\n" + "i:resourceDirs:\n"
				+ "i:classLocations:\n" + "  dep1\n" + "  dep2\n"
				+ "p:javacOptions:\n" + "  -Xlint\n" + "  -Xlint:-serial\n"
				+ "p:encoding:\n" + "  UTF-8\n" + "",
				target.contentDescriptor());
	}

	@Test
	public void emptySourceDirectoryProducesEmptyClasses() throws Exception {
		File srcDir = new File(wsRoot, "src");
		Iwant.mkdirs(srcDir);
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("empty").srcDirs(src)
				.classLocations().end();

		target.path(ctx);

		assertEquals("[]", Arrays.toString(new File(cached, "empty").list()));
	}

	@Test
	public void sourceDirectoryWithJustDotKeepInItProducesEmptyClasses()
			throws Exception {
		wsRootHasFile("src/.keep", "");

		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("empty").srcDirs(src)
				.classLocations().end();

		target.path(ctx);

		assertEquals("[]", Arrays.toString(new File(cached, "empty").list()));
	}

	@Test
	public void missingSourceDirectoryCausesFriendlyError() throws Exception {
		Source src = Source.underWsroot("missing-src");
		Target target = JavaClasses.with().name("missing").srcDirs(src)
				.classLocations().end();

		try {
			target.path(ctx);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Source directory does not exist: "
					+ new File(wsRoot, "missing-src"), e.getMessage());
		}
	}

	@Test
	public void usingNonDirectoryAsSourceDirectoryCausesFriendlyError()
			throws Exception {
		Iwant.mkdirs(wsRoot);
		File srcFile = new File(wsRoot, "Valid.java");
		Iwant.textFileEnsuredToHaveContent(srcFile, "class Valid {}");
		Source src = Source.underWsroot("Valid.java");
		Target target = JavaClasses.with().name("non-dir").srcDirs(src)
				.classLocations().end();

		try {
			target.path(ctx);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Source is not a directory: " + srcFile,
					e.getMessage());
		}
	}

	@Test
	public void debugInformationIsIncludedIffRequested() throws Exception {
		String srcDirName = "src";
		Path src = Source.underWsroot(srcDirName);
		Iwant.textFileEnsuredToHaveContent(
				new File(new File(wsRoot, srcDirName), "Foo.java"),
				"public class Foo {\n"
						+ "  public String hello(String message) {\n"
						+ "    final String greeting = \"Moi \";\n"
						+ "    return greeting +\" \" + message;\n" + "  }\n"
						+ "}\n");

		Target noDebug = JavaClasses.with().name("no-debug").srcDirs(src)
				.classLocations().end();
		noDebug.path(ctx);
		Target debug = JavaClasses.with().name("debug").srcDirs(src)
				.classLocations().debug(true).end();
		debug.path(ctx);

		byte[] noDebugContent = FileUtil
				.contentAsBytes(new File(ctx.cached(noDebug), "Foo.class"));
		assertFalse(TestArea.bytesContain(noDebugContent, "message"));
		assertFalse(TestArea.bytesContain(noDebugContent, "greeting"));

		byte[] debugContent = FileUtil
				.contentAsBytes(new File(ctx.cached(debug), "Foo.class"));
		assertTrue(TestArea.bytesContain(debugContent, "message"));
		assertTrue(TestArea.bytesContain(debugContent, "greeting"));
	}

	@Test
	public void resourceDirDefinitionAndGetter() {
		JavaClasses c = JavaClasses.with()
				.resourceDirs(Source.underWsroot("res")).end();
		assertEquals("[res]", c.resourceDirs().toString());
	}

	@Test
	public void clearingResourceDirsAndSpecifyingManyOfThem() {
		JavaClasses c = JavaClasses.with()
				.resourceDirs(Source.underWsroot("to-be-removed"))
				.noResourceDirs().resourceDirs(Source.underWsroot("r1"),
						Source.underWsroot("r2"))
				.end();
		assertEquals("[r1, r2]", c.resourceDirs().toString());
	}

	@Test
	public void resourcesAreCopiedAlongsideCompilationFromTheOneDirectoryGiven()
			throws Exception {
		wsRootHasFile("src/Foo.java", "public class Foo {}");
		wsRootHasFile("res/res.txt", "res.txt content");

		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src"))
				.resourceDirs(Source.underWsroot("res")).end();
		classes.path(ctx);

		assertTrue(new File(cached, "classes/Foo.class").exists());
		assertEquals("res.txt content", contentOfCached(classes, "res.txt"));
	}

	@Test
	public void missingResourceDirectoryIsIgnoredWithWarningAndWithoutThrowing()
			throws Exception {
		wsRootHasFile("src/Foo.java", "public class Foo {}");
		// wsroot does not have directory "res1"
		wsRootHasFile("res2/res2.txt", "res2.txt content");

		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src"))
				.resourceDirs(Source.underWsroot("res1"),
						Source.underWsroot("res2"))
				.end();
		classes.path(ctx);

		assertTrue(new File(cached, "classes/Foo.class").exists());
		assertEquals("res2.txt content", contentOfCached(classes, "res2.txt"));

		assertEquals(
				"WARNING: Missing resource dir: " + wsRoot + "/res1\n" + "",
				err());
	}

	@Test
	public void twoResourceDirsAndNoSrc() throws Exception {
		Iwant.textFileEnsuredToHaveContent(
				new File(wsRoot, "res1/pak1/res1.txt"), "res1.txt content");
		Iwant.textFileEnsuredToHaveContent(
				new File(wsRoot, "res2/pak2/res2.txt"), "res2.txt content");

		JavaClasses classes = JavaClasses.with().name("classes").resourceDirs(
				Source.underWsroot("res1"), Source.underWsroot("res2")).end();
		classes.path(ctx);

		assertEquals("res1.txt content",
				contentOfCached(classes, "pak1/res1.txt"));
		assertEquals("res2.txt content",
				contentOfCached(classes, "pak2/res2.txt"));
	}

	@Test
	public void defaultCharsetIsUtf8() {
		assertEquals(StandardCharsets.UTF_8,
				JavaClasses.with().end().encoding());
	}

	@Test
	public void overridingChracterEncoding() throws Exception {
		Charset differentCharset = Charset.forName("ISO-8859-1");
		assertFalse(differentCharset.equals(Charset.defaultCharset()));

		wsRootHasDirectory("src");
		StringBuilder java = new StringBuilder();
		java.append("public class Main {\n");
		java.append("  public static void main(String[] args) {\n");
		java.append("    System.out.print(\"aumlaut:ä\\n\");\n");
		java.append("  \n}");
		java.append("}\n");
		byte[] javaBytes = java.toString().getBytes(differentCharset);
		wsRootHasFile("src/Main.java", javaBytes);

		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src")).encoding(differentCharset)
				.end();
		classes.path(ctx);

		Iwant.runJavaMain(false, true, "Main",
				Arrays.asList(ctx.cached(classes)));

		assertEquals("aumlaut:ä\n", out());
	}

	@Test
	public void defaultJavacOptionsPassedToServices() throws Exception {
		wsRootHasDirectory("src");
		wsRootHasFile("src/Whatever.java", "public class Whatever {}");
		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src")).end();

		classes.path(ctx);

		assertEquals("[-Xlint, -Xlint:-serial]",
				ctx.iwant().lastJavacOptions().toString());
	}

	@Test
	public void customArgs() throws Exception {
		wsRootHasDirectory("src");
		wsRootHasFile("src/Whatever.java", "public class Whatever {}");
		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src"))
				.rawArgs("-cp", "cploc0:cploc1").end();

		classes.path(ctx);

		assertEquals("[-Xlint, -Xlint:-serial, -cp, cploc0:cploc1]",
				ctx.iwant().lastJavacOptions().toString());
	}

	@Test
	public void differentJavacOptionsPassedToServices() throws Exception {
		wsRootHasDirectory("src");
		wsRootHasFile("src/Whatever.java", "public class Whatever {}");
		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src")).debug(true)
				.sourceVersion(JavaCompliance.JAVA_1_7).end();

		classes.path(ctx);

		assertEquals("[-Xlint, -Xlint:-serial, -source, 1.7, -g]",
				ctx.iwant().lastJavacOptions().toString());
	}

	@Test
	public void javacOptionsAreInDescriptor() {
		assertTrue(JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src")).debug(true)
				.sourceVersion(JavaCompliance.JAVA_1_6).end()
				.contentDescriptor().contains("  -source\n  1.6"));
		assertTrue(JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src")).debug(true)
				.sourceVersion(JavaCompliance.JAVA_1_7).end()
				.contentDescriptor().contains("  -source\n  1.7\n  -g"));
		assertTrue(JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src")).debug(true)
				.sourceVersion(JavaCompliance.JAVA_1_8).end()
				.contentDescriptor().contains("  -source\n  1.8\n  -g"));
		assertTrue(JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src")).debug(true)
				.sourceVersion(JavaCompliance.JAVA_11).end().contentDescriptor()
				.contains("  --release\n  11\n  -g"));
	}

	@Test
	public void java8CompilesWithWarningsAboutBootclasspath() throws Exception {
		wsRootHasFile("src/UsingJ8.java", "class UsingJ8 {" + "	static {\n"
				+ "		java.util.Arrays.asList(\"1\")."
				+ "forEach(s -> System.out.println(s));\n" + "	}\n" + "}");
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("valid").srcDirs(src)
				.sourceVersion(JavaCompliance.JAVA_1_8).classLocations().end();

		target.path(ctx);

		assertTrue(new File(cached, "valid/UsingJ8.class").exists());
		assertEquals(
				"warning: [options] bootstrap class path not set in conjunction with -source 8\n"
						+ "1 warning\n" + "",
				err());
	}

	@Test
	public void java11CompilesWithoutWarnings() throws Exception {
		wsRootHasFile("src/UsingJ11.java",
				"class UsingJ11 {void s() {var s = \"\";System.out.println(s);}}");
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("valid").srcDirs(src)
				.sourceVersion(JavaCompliance.JAVA_11).classLocations().end();

		target.path(ctx);

		assertTrue(new File(cached, "valid/UsingJ11.class").exists());
		assertEquals("", err());
	}

	@Test
	public void java17CompilesWithoutWarnings() throws Exception {
		wsRootHasFile("src/UsingJ17.java",
				"public record UsingJ17(String a, int b) {}");
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("valid").srcDirs(src)
				.sourceVersion(JavaCompliance.JAVA_17).classLocations().end();

		target.path(ctx);

		assertTrue(new File(cached, "valid/UsingJ17.class").exists());
		assertEquals("", err());
	}

}
