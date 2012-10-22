package net.sf.iwant.entry3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.JavaClasses;
import net.sf.iwant.api.Source;
import net.sf.iwant.api.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.IwantNetworkMock;

public class Iwant3Test extends TestCase {

	private static final String LINE_SEPARATOR_KEY = "line.separator";

	private IwantEntry3TestArea testArea;
	private IwantNetworkMock network;
	private Iwant3 iwant3;
	private File asTest;
	private SecurityManager origSecman;
	private InputStream originalIn;
	private PrintStream originalOut;
	private PrintStream originalErr;
	private ByteArrayOutputStream out;
	private ByteArrayOutputStream err;
	private String originalLineSeparator;

	@Override
	public void setUp() throws Exception {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant3 = Iwant3.using(network);
		asTest = new File(testArea.root(), "as-test");
		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
		originalLineSeparator = System.getProperty(LINE_SEPARATOR_KEY);
		System.setProperty(LINE_SEPARATOR_KEY, "\n");
		startOfOutAndErrCapture();
	}

	private void startOfOutAndErrCapture() {
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}

	private String out() {
		return out.toString();
	}

	private String err() {
		return err.toString();
	}

	private String errIgnoringDebugLog() {
		return err.toString().replaceAll("\\([^\n]*\n", "");
	}

	@Override
	public void tearDown() {
		System.setSecurityManager(origSecman);
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setProperty(LINE_SEPARATOR_KEY, originalLineSeparator);

		if (!out().isEmpty()) {
			System.err.println("=== out:\n" + out());
		}
		if (!err().isEmpty()) {
			System.err.println("=== err:\n" + err());
		}
	}

	private void evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation()
			throws Exception {
		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals("I created " + asTest + "/i-have/ws-info\n"
					+ "Please edit it and rerun me.", e.getMessage());
		}
		assertEquals("# paths are relative to this file's directory\n"
				+ "WSNAME=example\n" + "WSROOT=../..\n"
				+ "WSDEF_SRC=wsdefdef\n"
				+ "WSDEF_CLASS=com.example.wsdefdef.WorkspaceProvider\n",
				testArea.contentOf("as-test/i-have/ws-info"));
	}

	public void testMissingAsSomeoneCausesFriendlyFailureAndExampleCreation()
			throws Exception {
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	public void testMissingIHaveCausesFriendlyFailureAndExampleCreation()
			throws Exception {
		testArea.newDir("as-test");
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	public void testMissingWsInfoCausesFriendlyFailureAndExampleCreation()
			throws Exception {
		testArea.newDir("as-test/i-have");
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	public void testInvalidWsInfoCausesFailure() throws Exception {
		testArea.hasFile("as-test/i-have/ws-info", "invalid\n");
		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals("Please specify WSNAME in " + asTest
					+ "/i-have/ws-info", e.getMessage());
		}
	}

	public void testMissingWsdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefCreation()
			throws Exception {
		testArea.hasFile("as-test/i-have/ws-info", "WSNAME=example\n"
				+ "WSROOT=../..\n" + "WSDEF_SRC=wsdefdef\n"
				+ "WSDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");
		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals(
					"I created\n"
							+ asTest
							+ "/i-have/wsdefdef/com/example/wsdefdef/ExampleWsProvider.java\n"
							+ "and\n" + asTest
							+ "/i-have/wsdef/com/example/wsdef/Workspace.java"
							+ "\nPlease edit them and rerun me.",
					e.getMessage());
		}
		String wsdefdefContent = testArea
				.contentOf("as-test/i-have/wsdefdef/com/example/wsdefdef/ExampleWsProvider.java");
		assertTrue(wsdefdefContent
				.startsWith("package com.example.wsdefdef;\n"));
		assertTrue(wsdefdefContent.contains(" class ExampleWsProvider "));
		// full content will be asserted by functionality
		String wsdefContent = testArea
				.contentOf("as-test/i-have/wsdef/com/example/wsdef/Workspace.java");
		assertTrue(wsdefContent.startsWith("package com.example.wsdef;\n"));
		assertTrue(wsdefContent.contains(" class Workspace "));
		// full content will be asserted by functionality
	}

	public void testIwant3AlsoCreatesWishScriptsForExampleWsDef()
			throws Exception {
		testMissingWsdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefCreation();
		// targets:
		assertTrue(testArea
				.contentOf("as-test/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"as-test/with/bash/iwant/target/hello/as-path").startsWith(
				"#!/bin/bash\n"));
		// side-effects:
		assertTrue(testArea.contentOf(
				"as-test/with/bash/iwant/list-of/side-effects").startsWith(
				"#!/bin/bash\n"));
		assertTrue(testArea
				.contentOf(
						"as-test/with/bash/iwant/side-effect/eclipse-settings/effective")
				.startsWith("#!/bin/bash\n"));
	}

	public void testListOfTargetsOfExampleWsDef() throws Exception {
		testMissingWsdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "list-of/targets");

		assertEquals("hello\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	public void testListOfSideEffectsOfExampleWsDef() throws Exception {
		testMissingWsdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "list-of/side-effects");

		assertEquals("eclipse-settings\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	private static String modifiedExampleWsDef() {
		StringBuilder wsdef = new StringBuilder();
		wsdef.append("package com.example.wsdef;\n");
		wsdef.append("\n");
		wsdef.append("import java.util.Arrays;\n");
		wsdef.append("import java.util.List;\n");
		wsdef.append("import net.sf.iwant.api.EclipseSettings;\n");
		wsdef.append("import net.sf.iwant.api.HelloTarget;\n");
		wsdef.append("import net.sf.iwant.api.IwantWorkspace;\n");
		wsdef.append("import net.sf.iwant.api.SideEffect;\n");
		wsdef.append("import net.sf.iwant.api.Target;\n");
		wsdef.append("\n");
		wsdef.append("public class ExampleWs implements IwantWorkspace {\n");
		wsdef.append("\n");
		wsdef.append("  @Override\n");
		wsdef.append("	public List<? extends Target> targets() {\n");
		wsdef.append("		return Arrays.asList(new HelloTarget(\"modified-hello\", \"content 1\"),\n");
		wsdef.append("			new HelloTarget(\"hello2\", \"content 2\"));\n");
		wsdef.append("	}\n");
		wsdef.append("\n");
		wsdef.append("  @Override\n");
		wsdef.append("  public List<? extends SideEffect> sideEffects() {\n");
		wsdef.append("		return Arrays.asList(EclipseSettings.with().name(\"eclipse-settings\").end());");
		wsdef.append("  }\n");
		wsdef.append("\n");
		wsdef.append("}\n");
		return wsdef.toString();
	}

	private static String exampleWsProvider() {
		StringBuilder b = new StringBuilder();
		b.append("package com.example.wsdefdef;\n");
		b.append("\n");
		b.append("import java.util.Arrays;\n");
		b.append("\n");
		b.append("import net.sf.iwant.api.IwantWorkspaceProvider;\n");
		b.append("import net.sf.iwant.api.JavaClasses;\n");
		b.append("import net.sf.iwant.api.Path;\n");
		b.append("import net.sf.iwant.api.Source;\n");
		b.append("\n");
		b.append("public class ExampleWsProvider implements IwantWorkspaceProvider {\n");
		b.append("\n");
		b.append("      @Override\n");
		b.append("      public JavaClasses workspaceClasses(Path iwantApiClasses) {\n");
		b.append("              return new JavaClasses(\"workspaceClasses\", workspaceSrc(), Arrays.asList(iwantApiClasses));\n");
		b.append("      }\n");
		b.append("\n");
		b.append("      private static Source workspaceSrc() {\n");
		b.append("              return Source.underWsroot(\"as-test/i-have/wsdef\");\n");
		b.append("      }\n");
		b.append("\n");
		b.append("      @Override\n");
		b.append("      public String workspaceClassname() {\n");
		b.append("              return \"com.example.wsdef.ExampleWs\";\n");
		b.append("      }\n");
		b.append("\n");
		b.append("}\n");
		return b.toString();
	}

	public void testListOfTargetsOfModifiedWsDef() throws Exception {
		testArea.hasFile("as-test/i-have/ws-info", "WSNAME=example\n"
				+ "WSROOT=../..\n" + "WSDEF_SRC=wsdefdef\n"
				+ "WSDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");
		testArea.hasFile(
				"as-test/i-have/wsdefdef/com/example/wsdefdef/ExampleWsProvider.java",
				exampleWsProvider());
		testArea.hasFile(
				"as-test/i-have/wsdef/com/example/wsdef/ExampleWs.java",
				modifiedExampleWsDef());

		iwant3.evaluate(asTest, "list-of/targets");

		assertEquals("modified-hello\nhello2\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	public void testListOfTargetsOfModifiedWsDefAlsoCreatesWishScripts()
			throws Exception {
		testListOfTargetsOfModifiedWsDef();

		assertTrue(testArea
				.contentOf("as-test/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"as-test/with/bash/iwant/target/modified-hello/as-path")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"as-test/with/bash/iwant/target/hello2/as-path").startsWith(
				"#!/bin/bash\n"));

		assertFalse(new File(testArea.root(),
				"as-test/with/bash/iwant/target/hello/as-path").exists());
	}

	public void testTargetHelloAsPathOfExampleWsDef() throws Exception {
		testMissingWsdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "target/hello/as-path");

		File cached = new File(asTest, ".i-cached/target/hello");
		assertEquals(cached + "\n", out());
		assertEquals("", errIgnoringDebugLog());

		assertEquals("hello from iwant", testArea.contentOf(cached));
	}

	public void testSideEffectEclipseSettingsEffectiveOfExampleWsDef()
			throws Exception {
		testMissingWsdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "side-effect/eclipse-settings/effective");

		assertEquals("", out());
		assertEquals("", errIgnoringDebugLog());

		assertTrue(testArea.contentOf(".project").contains(
				"<projectDescription>\n        <name>example</name>"));

		assertTrue(testArea.contentOf(".classpath").contains("<classpath>"));
		assertTrue(testArea.contentOf(".classpath").contains(
				"<classpathentry kind=\"src\" "
						+ "path=\"as-test/i-have/wsdef\"/>"));
		assertTrue(testArea.contentOf(".classpath").contains(
				"<classpathentry kind=\"src\" "
						+ "path=\"as-test/i-have/wsdefdef\"/>"));

		assertTrue(testArea
				.contentOf(".settings/org.eclipse.jdt.core.prefs")
				.contains(
						"org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6\n"));

		assertTrue(testArea.contentOf(".settings/org.eclipse.jdt.ui.prefs")
				.contains("formatter_profile=_iwant-generated\n"));
	}

	public void testTargetModifiedHelloAsPathOfModifiedWsDef() throws Exception {
		testArea.hasFile("as-test/i-have/ws-info", "WSNAME=example\n"
				+ "WSROOT=../..\n" + "WSDEF_SRC=wsdefdef\n"
				+ "WSDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");
		testArea.hasFile(
				"as-test/i-have/wsdefdef/com/example/wsdefdef/ExampleWsProvider.java",
				exampleWsProvider());
		testArea.hasFile(
				"as-test/i-have/wsdef/com/example/wsdef/ExampleWs.java",
				modifiedExampleWsDef());

		iwant3.evaluate(asTest, "target/modified-hello/as-path");

		File cached = new File(asTest, ".i-cached/target/modified-hello");
		assertEquals(cached + "\n", out());
		assertEquals("", errIgnoringDebugLog());

		assertEquals("content 1", testArea.contentOf(cached));
	}

	public void testListOfTargetsFailsIfWsDefDoesNotCompile() throws Exception {
		testArea.hasFile("as-test/i-have/ws-info", "WSNAME=example\n"
				+ "WSROOT=../..\n" + "WSDEF_SRC=wsdef\n"
				+ "WSDEF_CLASS=com.example.wsdef.ExampleWs\n");
		testArea.hasFile(
				"as-test/i-have/wsdef/com/example/wsdef/ExampleWs.java",
				"crap\n");

		try {
			iwant3.evaluate(asTest, "list-of/targets");
			fail();
		} catch (IwantException e) {
			assertEquals("Compilation failed.", e.getMessage());
		}
		assertEquals("", out());
		assertEquals(testArea.root()
				+ "/as-test/i-have/wsdef/com/example/wsdef/ExampleWs.java"
				+ ":1: reached end of file while parsing\n" + "crap\n" + "^\n"
				+ "1 error\n", errIgnoringDebugLog());
	}

	/**
	 * A learning test: the interface to cast to must come from the classloader
	 * of the running class, not by loading it again.
	 */
	public void testToLearnThatEvenTwoInstancesOfSameClassloaderLoadIncompatibleClasses()
			throws Exception {
		File classes = new File(Iwant3.class.getResource(
				"/net/sf/iwant/api/IwantWorkspace.class").toURI())
				.getParentFile().getParentFile().getParentFile()
				.getParentFile().getParentFile();

		List<File> locations = Arrays.asList(classes);
		String className = IwantWorkspace.class.getCanonicalName();
		Class<?> c1 = Iwant.classLoader(true, locations).loadClass(className);
		Class<?> c2 = Iwant.classLoader(true, locations).loadClass(className);
		assertEquals(className, c1.getCanonicalName());
		assertEquals(className, c2.getCanonicalName());
		assertFalse(c1 == c2);
		assertFalse(IwantWorkspace.class.isAssignableFrom(c1));
		assertFalse(c1.isAssignableFrom(c2));
	}

	/**
	 * Corresponds to calling help.sh once more
	 */
	public void testEmptyWishAfterCreationOfExampleWsDef() throws Exception {
		testMissingWsdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefCreation();
		startOfOutAndErrCapture();

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals("Try " + asTest + "/with/bash/iwant/list-of/targets",
					e.getMessage());
		}

		assertEquals("", out());
	}

	/**
	 * TODO extract class for this functionality and move these to its test, the
	 * setup is very different from other tests here.
	 */
	public void testWsdefRuntimeClasspathWhenWsdefClassesTargetDefinesNoExtra() {
		File wsRoot = testArea.newDir("wsroot");
		CachesMock caches = new CachesMock(wsRoot);
		TargetEvaluationContextMock ctx = new TargetEvaluationContextMock(
				Iwant.using(network), caches);
		caches.cachesModifiableTargetsAt(new File("cached"));

		JavaClasses wsdDefClassesTarget = new JavaClasses("wsdef",
				Source.underWsroot("wsdef"), Arrays.asList(new TargetMock(
						"iwant-api-classes")));

		File wsDefdefClasses = new File("wsDefdefClasses");
		File wsDefClasses = new File("wsDefClasses");
		List<File> cp = Iwant3.wsdefRuntimeClasspath(ctx, wsdDefClassesTarget,
				wsDefdefClasses, wsDefClasses);

		assertEquals(
				"[wsDefdefClasses, wsDefClasses, cached/iwant-api-classes]",
				cp.toString());
	}

	public void testWsdefRuntimeClasspathWhenWsdefClassesTargetDefinesAnExternalLibrary() {
		File wsRoot = testArea.newDir("wsroot");
		CachesMock caches = new CachesMock(wsRoot);
		TargetEvaluationContextMock ctx = new TargetEvaluationContextMock(
				Iwant.using(network), caches);
		caches.cachesModifiableTargetsAt(new File("cached"));

		JavaClasses wsdDefClassesTarget = new JavaClasses("wsdef",
				Source.underWsroot("wsdef"), Arrays.asList(new TargetMock(
						"iwant-api-classes"),
						new TargetMock("external-library")));

		File wsDefdefClasses = new File("wsDefdefClasses");
		File wsDefClasses = new File("wsDefClasses");
		List<File> cp = Iwant3.wsdefRuntimeClasspath(ctx, wsdDefClassesTarget,
				wsDefdefClasses, wsDefClasses);

		assertEquals("[wsDefdefClasses, wsDefClasses,"
				+ " cached/iwant-api-classes, cached/external-library]",
				cp.toString());
	}

}
