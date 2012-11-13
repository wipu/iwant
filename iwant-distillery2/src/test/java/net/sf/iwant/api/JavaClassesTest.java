package net.sf.iwant.api;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.IwantNetworkMock;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.entry3.IwantEntry3TestArea;
import net.sf.iwant.entry3.TargetMock;

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

	public void testSrcDirIsAnIgredient() throws Exception {
		Path src = Source.underWsroot("src");
		Target target = new JavaClasses("classes", src,
				Collections.<Path> emptyList());

		assertTrue(target.ingredients().contains(src));
	}

	public void testSrcDirIsInContentDescriptor() throws Exception {
		assertEquals("net.sf.iwant.api.JavaClasses {\n  src:src\n}",
				new JavaClasses("classes", Source.underWsroot("src"),
						Collections.<Path> emptyList()).contentDescriptor());
		assertEquals("net.sf.iwant.api.JavaClasses {\n  src:src2\n}",
				new JavaClasses("classes2", Source.underWsroot("src2"),
						Collections.<Path> emptyList()).contentDescriptor());
	}

	public void testCrapToPathFails() throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		new FileWriter(new File(srcDir, "Crap.java")).append("crap").close();
		Source src = Source.underWsroot("src");
		Target target = new JavaClasses("crap", src,
				Collections.<Path> emptyList());

		try {
			target.path(ctx);
			fail();
		} catch (IwantException e) {
			assertEquals("Compilation failed.", e.getMessage());
		}
	}

	public void testValidToPathCompiles() throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		new FileWriter(new File(srcDir, "Valid.java")).append("class Valid {}")
				.close();
		Source src = Source.underWsroot("src");
		Target target = new JavaClasses("valid", src,
				Collections.<Path> emptyList());

		target.path(ctx);

		assertTrue(new File(cached, "valid/Valid.class").exists());
	}

	public void testToPathCompilesFromMultiplePackages() throws Exception {
		File srcDir = new File(wsRoot, "src");
		new File(srcDir, "pak1").mkdirs();
		new File(srcDir, "pak2").mkdirs();
		new FileWriter(new File(srcDir, "Caller.java")).append(
				"class Caller {pak1.Callee1 callee1;pak2.Callee2 callee2;}")
				.close();
		new FileWriter(new File(srcDir, "pak1/Callee1.java")).append(
				"package pak1;\npublic class Callee1 {}").close();
		new FileWriter(new File(srcDir, "pak2/Callee2.java")).append(
				"package pak2;\npublic class Callee2 {}").close();
		Source src = Source.underWsroot("src");
		Target target = new JavaClasses("multiple", src,
				Collections.<Path> emptyList());

		target.path(ctx);

		assertTrue(new File(cached, "multiple/Caller.class").exists());
		assertTrue(new File(cached, "multiple/pak1/Callee1.class").exists());
		assertTrue(new File(cached, "multiple/pak2/Callee2.class").exists());
	}

	public void testClassWithDepToClassesCompiles() throws Exception {
		File superClassFile = new File(getClass().getResource(
				"SuperClassForJavaClassesTest.class").toURI());
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		new FileWriter(new File(srcDir, "Subclass.java")).append(
				"class Subclass extends "
						+ SuperClassForJavaClassesTest.class.getCanonicalName()
						+ "{}").close();
		Source src = Source.underWsroot("src");
		File superClassClasses = superClassFile.getParentFile().getParentFile()
				.getParentFile().getParentFile().getParentFile()
				.getAbsoluteFile();
		Target target = new JavaClasses("valid", src,
				Arrays.asList(new ExternalSource(superClassClasses)));

		target.path(ctx);

		assertTrue(new File(cached, "valid/Subclass.class").exists());
	}

	public void testDependenciesAreIgredients() {
		Target dep1 = new TargetMock("dep1");
		Target dep2 = new TargetMock("dep2");
		Target target = new JavaClasses("valid", Source.underWsroot("src"),
				Arrays.asList(dep1, dep2));

		assertTrue(target.ingredients().contains(dep1));
		assertTrue(target.ingredients().contains(dep2));
	}

	public void testDependenciesAreInContentDescriptor() {
		Target dep1 = new TargetMock("dep1");
		Target dep2 = new TargetMock("dep2");
		Target target = new JavaClasses("valid", Source.underWsroot("src"),
				Arrays.asList(dep1, dep2));

		assertEquals("net.sf.iwant.api.JavaClasses {\n  src:src\n"
				+ "  classes:dep1\n  classes:dep2\n" + "}",
				target.contentDescriptor());
	}

	public void testEmptySourceDirectoryProducesEmptyClasses() throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		Source src = Source.underWsroot("src");
		Target target = new JavaClasses("empty", src,
				Collections.<Path> emptyList());

		target.path(ctx);

		assertEquals("[]", Arrays.toString(new File(cached, "empty").list()));
	}

	public void testSourceDirectoryWithJustDotKeepInItProducesEmptyClasses()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		testArea.hasFile("src/.keep", "");

		Source src = Source.underWsroot("src");
		Target target = new JavaClasses("empty", src,
				Collections.<Path> emptyList());

		target.path(ctx);

		assertEquals("[]", Arrays.toString(new File(cached, "empty").list()));
	}

	public void testMissingSourceDirectoryCausesFriendlyError()
			throws Exception {
		Source src = Source.underWsroot("missing-src");
		Target target = new JavaClasses("missing", src,
				Collections.<Path> emptyList());

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
		new FileWriter(srcFile).append("class Valid {}").close();
		Source src = Source.underWsroot("Valid.java");
		Target target = new JavaClasses("non-dir", src,
				Collections.<Path> emptyList());

		try {
			target.path(ctx);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Source is not a directory: " + srcFile,
					e.getMessage());
		}
	}

}
