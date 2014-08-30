package net.sf.iwant.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClasses.JavaClassesSpex;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.coreservices.StreamUtil;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry3.TargetMock;
import net.sf.iwant.testarea.TestArea;

public class JavaClassesTest extends IwantTestCase {

	public void testSrcDirIsAnIgredient() {
		Path src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("classes").srcDirs(src)
				.classLocations().end();

		assertTrue(target.ingredients().contains(src));
	}

	public void testSrcDirsAreIgredients() {
		Path src1 = Source.underWsroot("src1");
		Path src2 = Source.underWsroot("src1");
		Target target = JavaClasses.with().name("classes").srcDirs(src1, src2)
				.classLocations().end();

		assertTrue(target.ingredients().contains(src1));
		assertTrue(target.ingredients().contains(src2));
	}

	public void testResourceDirsAreIgredients() {
		Path res1 = Source.underWsroot("res1");
		Path res2 = Source.underWsroot("res1");
		Target target = JavaClasses.with().name("classes")
				.resourceDirs(res1, res2).classLocations().end();

		assertTrue(target.ingredients().contains(res1));
		assertTrue(target.ingredients().contains(res2));
	}

	public void testSrcDirsCanBeEmptiedDuringSpecifyingJavaClasses() {
		Path src1 = Source.underWsroot("src1");
		Path src2 = Source.underWsroot("src1");
		JavaClassesSpex spex = JavaClasses.with().name("classes").srcDirs(src1)
				.classLocations();

		spex = spex.noSrcDirs().srcDirs(src2);

		JavaClasses target = spex.end();

		assertFalse(target.ingredients().contains(src1));
		assertTrue(target.ingredients().contains(src2));
	}

	public void testRelevantSettingsAreMentionedInContentDescriptor() {
		assertEquals(
				"net.sf.iwant.api.javamodules.JavaClasses {\n" + "  src:src\n"
						+ "  debug:false\n" + "  encoding:UTF-8\n" + "}",
				JavaClasses.with().name("classes")
						.srcDirs(Source.underWsroot("src")).classLocations()
						.debug(false).encoding(Charset.forName("UTF-8")).end()
						.contentDescriptor());
		assertEquals(
				"net.sf.iwant.api.javamodules.JavaClasses {\n" + "  src:src2\n"
						+ "  src:src3\n" + "  res:res\n" + "  debug:true\n"
						+ "  encoding:ISO-8859-1\n" + "}",
				JavaClasses
						.with()
						.name("classes2")
						.srcDirs(Source.underWsroot("src2"),
								Source.underWsroot("src3"))
						.resourceDirs(Source.underWsroot("res"))
						.classLocations().debug(true)
						.encoding(Charset.forName("ISO-8859-1")).end()
						.contentDescriptor());
	}

	public void testCrapToPathFails() throws Exception {
		File srcDir = new File(wsRoot, "src");
		Iwant.newTextFile(new File(srcDir, "Crap.java"), "crap");
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

	public void testValidToPathCompiles() throws Exception {
		File srcDir = new File(wsRoot, "src");
		Iwant.newTextFile(new File(srcDir, "Valid.java"), "class Valid {}");
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("valid").srcDirs(src)
				.classLocations().end();

		target.path(ctx);

		assertTrue(new File(cached, "valid/Valid.class").exists());
	}

	public void testToPathCompilesFromMultiplePackages() throws Exception {
		File srcDir = new File(wsRoot, "src");
		Iwant.newTextFile(new File(srcDir, "Caller.java"),
				"class Caller {pak1.Callee1 callee1;pak2.Callee2 callee2;}");
		Iwant.newTextFile(new File(srcDir, "pak1/Callee1.java"),
				"package pak1;\npublic class Callee1 {}");
		Iwant.newTextFile(new File(srcDir, "pak2/Callee2.java"),
				"package pak2;\npublic class Callee2 {}");
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("multiple").srcDirs(src)
				.classLocations().end();

		target.path(ctx);

		assertTrue(new File(cached, "multiple/Caller.class").exists());
		assertTrue(new File(cached, "multiple/pak1/Callee1.class").exists());
		assertTrue(new File(cached, "multiple/pak2/Callee2.class").exists());
	}

	public void testToPathCompilesFromMultipleSrcDirs() throws Exception {
		File srcDir1 = new File(wsRoot, "src1");
		Iwant.newTextFile(new File(srcDir1, "Caller.java"),
				"class Caller {pak1.Callee1 callee1;pak2.Callee2 callee2;}");

		File srcDir2 = new File(wsRoot, "src2");
		Iwant.newTextFile(new File(srcDir2, "pak1/Callee1.java"),
				"package pak1;\npublic class Callee1 {}");
		File srcDir3 = new File(wsRoot, "src3");
		Iwant.newTextFile(new File(srcDir3, "pak2/Callee2.java"),
				"package pak2;\npublic class Callee2 {}");

		Target target = JavaClasses
				.with()
				.name("multiple")
				.srcDirs(Source.underWsroot("src1"),
						Source.underWsroot("src2"), Source.underWsroot("src3"))
				.classLocations().end();

		target.path(ctx);

		assertTrue(new File(cached, "multiple/Caller.class").exists());
		assertTrue(new File(cached, "multiple/pak1/Callee1.class").exists());
		assertTrue(new File(cached, "multiple/pak2/Callee2.class").exists());
	}

	public void testClassWithDepToClassesCompiles() throws Exception {
		File superClassFile = new File(getClass().getResource(
				"SuperClassForJavaClassesTest.class").toURI());
		File srcDir = new File(wsRoot, "src");
		Iwant.newTextFile(
				new File(srcDir, "Subclass.java"),
				"class Subclass extends "
						+ SuperClassForJavaClassesTest.class.getCanonicalName()
						+ "{}");
		Source src = Source.underWsroot("src");
		File superClassClasses = superClassFile.getParentFile().getParentFile()
				.getParentFile().getParentFile().getParentFile()
				.getAbsoluteFile();
		Target target = JavaClasses.with().name("valid").srcDirs(src)
				.classLocations(new ExternalSource(superClassClasses)).end();

		target.path(ctx);

		assertTrue(new File(cached, "valid/Subclass.class").exists());
	}

	public void testDependenciesAreIgredients() {
		Target dep1 = new TargetMock("dep1");
		Target dep2 = new TargetMock("dep2");
		Target target = JavaClasses.with().name("valid")
				.srcDirs(Source.underWsroot("src")).classLocations(dep1, dep2)
				.end();

		assertTrue(target.ingredients().contains(dep1));
		assertTrue(target.ingredients().contains(dep2));
	}

	public void testDependenciesAreInContentDescriptor() {
		Target dep1 = new TargetMock("dep1");
		Target dep2 = new TargetMock("dep2");
		Target target = JavaClasses.with().name("valid")
				.srcDirs(Source.underWsroot("src")).classLocations(dep1, dep2)
				.end();

		assertEquals("net.sf.iwant.api.javamodules.JavaClasses {\n"
				+ "  src:src\n" + "  classes:dep1\n" + "  classes:dep2\n"
				+ "  debug:false\n" + "  encoding:null\n" + "}",
				target.contentDescriptor());
	}

	public void testEmptySourceDirectoryProducesEmptyClasses() throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("empty").srcDirs(src)
				.classLocations().end();

		target.path(ctx);

		assertEquals("[]", Arrays.toString(new File(cached, "empty").list()));
	}

	public void testSourceDirectoryWithJustDotKeepInItProducesEmptyClasses()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		testArea.hasFile("src/.keep", "");

		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("empty").srcDirs(src)
				.classLocations().end();

		target.path(ctx);

		assertEquals("[]", Arrays.toString(new File(cached, "empty").list()));
	}

	public void testMissingSourceDirectoryCausesFriendlyError()
			throws Exception {
		Source src = Source.underWsroot("missing-src");
		Target target = JavaClasses.with().name("missing").srcDirs(src)
				.classLocations().end();

		try {
			target.path(ctx);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Source directory does not exist: " + wsRoot
					+ "/missing-src", e.getMessage());
		}
	}

	public void testUsingNonDirectoryAsSourceDirectoryCausesFriendlyError()
			throws Exception {
		wsRoot.mkdirs();
		File srcFile = new File(wsRoot, "Valid.java");
		Iwant.newTextFile(srcFile, "class Valid {}");
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

	public void testDebugInformationIsIncludedIffRequested() throws Exception {
		String srcDirName = "src";
		Path src = Source.underWsroot(srcDirName);
		Iwant.newTextFile(new File(new File(wsRoot, srcDirName), "Foo.java"),
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

		byte[] noDebugContent = FileUtil.contentAsBytes(new File(ctx
				.cached(noDebug), "Foo.class"));
		assertFalse(TestArea.bytesContain(noDebugContent, "message"));
		assertFalse(TestArea.bytesContain(noDebugContent, "greeting"));

		byte[] debugContent = FileUtil.contentAsBytes(new File(ctx
				.cached(debug), "Foo.class"));
		assertTrue(TestArea.bytesContain(debugContent, "message"));
		assertTrue(TestArea.bytesContain(debugContent, "greeting"));
	}

	public void testResourceDirDefinitionAndGetter() {
		JavaClasses c = JavaClasses.with()
				.resourceDirs(Source.underWsroot("res")).end();
		assertEquals("[res]", c.resourceDirs().toString());
	}

	public void testClearingResourceDirsAndSpecifyingManyOfThem() {
		JavaClasses c = JavaClasses
				.with()
				.resourceDirs(Source.underWsroot("to-be-removed"))
				.noResourceDirs()
				.resourceDirs(Source.underWsroot("r1"),
						Source.underWsroot("r2")).end();
		assertEquals("[r1, r2]", c.resourceDirs().toString());
	}

	public void testResourcesAreCopiedAlongsideCompilationFromTheOneDirectoryGiven()
			throws Exception {
		Iwant.newTextFile(new File(wsRoot, "src/Foo.java"),
				"public class Foo {}");
		Iwant.newTextFile(new File(wsRoot, "res/res.txt"), "res.txt content");

		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src"))
				.resourceDirs(Source.underWsroot("res")).end();
		classes.path(ctx);

		assertTrue(new File(cached, "classes/Foo.class").exists());
		assertEquals("res.txt content",
				testArea.contentOf(new File(cached, "classes/res.txt")));
	}

	public void testTwoResourceDirsAndNoSrc() throws Exception {
		Iwant.newTextFile(new File(wsRoot, "res1/pak1/res1.txt"),
				"res1.txt content");
		Iwant.newTextFile(new File(wsRoot, "res2/pak2/res2.txt"),
				"res2.txt content");

		JavaClasses classes = JavaClasses
				.with()
				.name("classes")
				.resourceDirs(Source.underWsroot("res1"),
						Source.underWsroot("res2")).end();
		classes.path(ctx);

		assertEquals("res1.txt content",
				testArea.contentOf(new File(cached, "classes/pak1/res1.txt")));
		assertEquals("res2.txt content",
				testArea.contentOf(new File(cached, "classes/pak2/res2.txt")));
	}

	public void testOverridingChracterEncoding() throws Exception {
		Charset differentCharset = Charset.forName("ISO-8859-1");
		assertFalse(differentCharset.equals(Charset.defaultCharset()));

		new File(wsRoot, "src").mkdirs();
		StringBuilder java = new StringBuilder();
		java.append("public class Main {\n");
		java.append("  public static void main(String[] args) {\n");
		java.append("    System.out.println(\"aumlaut:ä\");\n");
		java.append("  \n}");
		java.append("}\n");
		byte[] javaBytes = java.toString().getBytes(differentCharset);
		StreamUtil.pipeAndClose(new ByteArrayInputStream(javaBytes),
				new FileOutputStream(new File(wsRoot, "src/Main.java")));

		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src")).encoding(differentCharset)
				.end();
		classes.path(ctx);

		Iwant.runJavaMain(false, true, "Main",
				Arrays.asList(ctx.cached(classes)));

		assertEquals("aumlaut:ä\n", out());
	}

}
