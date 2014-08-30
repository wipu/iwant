package net.sf.iwant.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.entry.Iwant;

public class EmmaInstrumentationTest extends IwantTestCase {

	private Path downloaded(Path downloaded) throws IOException {
		return new ExternalSource(AsEmbeddedIwantUser.with()
				.workspaceAt(wsRoot).cacheAt(cacheDir).iwant()
				.target((Target) downloaded).asPath());
	}

	private Path emma() throws IOException {
		return downloaded(TestedIwantDependencies.emma());
	}

	private JavaClassesAndSources newJavaClassesAndSources(String name)
			throws Exception {
		String srcDirString = name + "-src";
		File srcDir = new File(wsRoot, srcDirString);
		Iwant.newTextFile(new File(srcDir, "Hello.java"),
				"public class Hello {\n"
						+ "  public static void main(String[] args) {\n"
						+ "    System.out.println(\"main\");\n" + "  }\n}\n");
		JavaClasses classes = JavaClasses.with().name(name + "-classes")
				.srcDirs(Source.underWsroot(srcDirString)).classLocations()
				.end();
		classes.path(ctx);
		return new JavaClassesAndSources(classes,
				Source.underWsroot(srcDirString));
	}

	// the tests

	public void testNameIsDerivedFromTheNameOfJavaClassesAndSourcesPair()
			throws IOException {
		assertEquals(
				"one.emma-instr",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("one"), Source
										.underWsroot("irrelevant")))
						.using(emma()).name());
		assertEquals(
				"two.emma-instr",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("two"), Source
										.underWsroot("irrelevant")))
						.using(emma()).name());
	}

	public void testIngredientsAreEmmaAndTheClassesIfNoFilter()
			throws IOException {
		assertEquals(
				"[" + emma() + ", one]",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("one"), Source
										.underWsroot("irrelevant")))
						.using(emma()).ingredients().toString());
		assertEquals(
				"[" + emma() + ", two]",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("two"), Source
										.underWsroot("irrelevant")))
						.using(emma()).ingredients().toString());
	}

	public void testIngredientsAreEmmaAndTheClassesAndTheFilterIfTherIsOne()
			throws IOException {
		assertEquals(
				"[" + emma() + ", one, emma-filter.txt]",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("one"), Source
										.underWsroot("irrelevant")))
						.filter(Source.underWsroot("emma-filter.txt"))
						.using(emma()).ingredients().toString());
	}

	public void testDescriptor() throws IOException {
		assertEquals(
				"net.sf.iwant.api.EmmaInstrumentation:[" + emma() + ", one]",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("one"), Source
										.underWsroot("irrelevant")))
						.using(emma()).contentDescriptor());
		assertEquals(
				"net.sf.iwant.api.EmmaInstrumentation:[" + emma() + ", two]",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("two"), Source
										.underWsroot("irrelevant")))
						.using(emma()).contentDescriptor());
	}

	public void testInstrumentationCreatesNeededFiles() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources("instrtest");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());

		instr.path(ctx);

		File instrDir = new File(cacheDir, "instrtest-classes.emma-instr");

		assertEquals("metadata.out.file=" + instrDir + "/emma.em\n"
				+ "verbosity.level=warning\n" + "coverage.out.file=" + instrDir
				+ "/please-override-when-running-tests.ec\n",
				testArea.contentOf(new File(instrDir, "emma-instr.properties")));
		assertTrue(new File(instrDir, "emma.em").exists());
		assertTrue(new File(instrDir, "instr-classes/Hello.class").exists());
	}

	/**
	 * This is an inconvenient feature of emma, producing "null" instead of
	 * "empty" for the metafile. EmmaInstrumentation works around another part
	 * of the problem and copies the excluded classes (including always excluded
	 * interfaces)
	 */
	public void testInstrumentationDoesNotCreateMetafileAtAllIfFilterAppliesToAllClassesButItDoesContainFilteredOutClassesEvenInterfaces()
			throws Exception {
		String srcDirString = "src";
		File srcDir = new File(wsRoot, srcDirString);
		Iwant.newTextFile(new File(srcDir,
				"nottoinstrument/NotToInstrument.java"),
				"package nottoinstrument;\npublic class NotToInstrument {}\n");
		Iwant.newTextFile(new File(srcDir,
				"packagetoinstrument/AnInterface.java"),
				"package packagetoinstrument;\npublic interface AnInterface {}\n");
		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot(srcDirString)).classLocations()
				.end();
		classes.path(ctx);
		JavaClassesAndSources classesAndSources = new JavaClassesAndSources(
				classes, Source.underWsroot(srcDirString));

		String filterFileString = "emma-filter.txt";
		Iwant.newTextFile(new File(wsRoot, filterFileString),
				"-nottoinstrument.*\n");

		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.filter(Source.underWsroot(filterFileString)).using(emma());

		instr.path(ctx);
		File instrDir = new File(cacheDir, "classes.emma-instr");

		assertFalse(new File(instrDir, "emma.em").exists());
		assertTrue(new File(instrDir,
				"instr-classes/nottoinstrument/NotToInstrument.class").exists());
		assertTrue(new File(instrDir,
				"instr-classes/packagetoinstrument/AnInterface.class").exists());
	}

	public void testLeavingClassByNameUninstrumentedByUsingAFilterFileMeansTheExcludedClassIsJustCopiedFromClasses()
			throws Exception {
		String srcDirString = "src";
		File srcDir = new File(wsRoot, srcDirString);
		Iwant.newTextFile(new File(srcDir, "toinstrument/ToInstrument.java"),
				"package toinstrument;\npublic class ToInstrument {}\n");
		Iwant.newTextFile(new File(srcDir,
				"nottoinstrument/NotToInstrument.java"),
				"package nottoinstrument;\npublic class NotToInstrument {}\n");
		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot(srcDirString)).classLocations()
				.end();
		classes.path(ctx);
		JavaClassesAndSources classesAndSources = new JavaClassesAndSources(
				classes, Source.underWsroot(srcDirString));

		String filterFileString = "emma-filter.txt";
		Iwant.newTextFile(new File(wsRoot, filterFileString),
				"-nottoinstrument.*\n");

		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.filter(Source.underWsroot(filterFileString)).using(emma());

		instr.path(ctx);

		File origIncluded = new File(ctx.cached(classes),
				"toinstrument/ToInstrument.class");
		File instrIncluded = new File(ctx.cached(instr),
				"instr-classes/toinstrument/ToInstrument.class");
		assertFalse(Arrays.equals(FileUtil.contentAsBytes(origIncluded),
				FileUtil.contentAsBytes(instrIncluded)));

		File origExcluded = new File(ctx.cached(classes),
				"nottoinstrument/NotToInstrument.class");
		File instrExcluded = new File(ctx.cached(instr),
				"instr-classes/nottoinstrument/NotToInstrument.class");
		assertTrue(Arrays.equals(FileUtil.contentAsBytes(origExcluded),
				FileUtil.contentAsBytes(instrExcluded)));
	}

	/**
	 * If property overrides don't work, the metafile gets created to cwd
	 */
	public void testInstrumentationDoesNotCreateMetafileToCwd()
			throws Exception {
		testInstrumentationCreatesNeededFiles();
		File cwd = new File(System.getProperty("user.dir"));
		assertFalse(new File(cwd, "coverage.em").exists());

	}

	public void testCreationFromJavaSrcModule() throws IOException {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.end();

		EmmaInstrumentation instr = EmmaInstrumentation.of(mod).using(emma());

		assertEquals("mod-main-classes.emma-instr", instr.name());
		assertEquals("mod-main-classes", instr.classesAndSources().classes()
				.toString());
		assertEquals("[mod/src]", instr.classesAndSources().sources()
				.toString());
	}

	public void testCreationFromJavaSrcModuleWithTestJavaOnly()
			throws IOException {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").testJava("test")
				.end();

		EmmaInstrumentation instr = EmmaInstrumentation.of(mod).using(emma());

		assertNull(instr);
	}

	public void testCreationFromJavaBinModule() throws IOException {
		JavaBinModule mod = JavaBinModule.providing(
				Source.underWsroot("lib.jar")).end();

		EmmaInstrumentation instr = EmmaInstrumentation.of(mod).using(emma());

		assertNull(instr);
	}

}
