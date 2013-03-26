package net.sf.iwant.api;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.entry3.TargetMock;
import net.sf.iwant.testing.IwantEntry3TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class JavaClassesTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private File cached;
	private CachesMock caches;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = new File(testArea.root(), "wsRoot");
		caches = new CachesMock(wsRoot);
		ctx = new TargetEvaluationContextMock(iwant, caches);
		ctx.hasWsRoot(wsRoot);
		cached = new File(testArea.root(), "cached");
		caches.cachesModifiableTargetsAt(cached);
	}

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

	public void testSrcDirIsInContentDescriptor() {
		assertEquals(
				"net.sf.iwant.api.JavaClasses {\n  src:src\n}",
				JavaClasses.with().name("classes")
						.srcDirs(Source.underWsroot("src")).classLocations()
						.end().contentDescriptor());
		assertEquals(
				"net.sf.iwant.api.JavaClasses {\n  src:src2\n}",
				JavaClasses.with().name("classes2")
						.srcDirs(Source.underWsroot("src2")).classLocations()
						.end().contentDescriptor());
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

		assertEquals("net.sf.iwant.api.JavaClasses {\n  src:src\n"
				+ "  classes:dep1\n  classes:dep2\n" + "}",
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

}
