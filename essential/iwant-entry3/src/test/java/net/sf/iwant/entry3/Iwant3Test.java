package net.sf.iwant.entry3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.apimocks.TargetMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry3.Iwant3.CombinedSrcFromUnmodifiableIwantWsRoot;
import net.sf.iwant.entrymocks.IwantNetworkMock;
import net.sf.iwant.iwantwsrootfinder.IwantWsRootFinder;
import net.sf.iwant.testarea.TestArea;

public class Iwant3Test extends TestCase {

	private static final String LINE_SEPARATOR_KEY = "line.separator";

	private TestArea testArea;
	private IwantNetworkMock network;
	private Iwant3 iwant3;
	private File wsRoot;
	private File asTest;
	private SecurityManager origSecman;
	private InputStream originalIn;
	private PrintStream originalOut;
	private PrintStream originalErr;
	private ByteArrayOutputStream out;
	private ByteArrayOutputStream err;
	private String originalLineSeparator;

	private File iwantWs;

	private File combinedIwantSrc;

	@Override
	public void setUp() throws Exception {
		testArea = TestArea.forTest(this);
		testArea.hasFile("wsroot/as-example-developer/with/bash/iwant/help.sh",
				"#!/bin/bash\njust a mock because this exists in real life\n");
		network = new IwantNetworkMock(testArea);
		combinedIwantSrc = new File(testArea.root(), "combined-iwant-src");
		iwantWs = IwantWsRootFinder.mockWsRoot();
		network.cachesAt(new CombinedSrcFromUnmodifiableIwantWsRoot(iwantWs),
				combinedIwantSrc);
		iwant3 = Iwant3.using(network, iwantWs);
		wsRoot = new File(testArea.root(), "wsroot");
		asTest = new File(wsRoot, "as-example-developer");
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
			assertEquals("I created " + asTest + "/i-have/conf/ws-info\n"
					+ "Please edit it and rerun me.", e.getMessage());
		}
		assertEquals(
				"# paths are relative to this file's directory\n"
						+ "WSNAME=example\n"
						+ "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.WorkspaceProvider\n",
				testArea.contentOf("wsroot/as-example-developer/i-have/conf/ws-info"));
	}

	public void testMissingAsSomeoneCausesFriendlyFailureAndExampleCreation()
			throws Exception {
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	public void testMissingIHaveCausesFriendlyFailureAndExampleCreation()
			throws Exception {
		testArea.newDir("as-example-developer");
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	public void testMissingWsInfoCausesFriendlyFailureAndExampleCreation()
			throws Exception {
		testArea.newDir("as-example-developer/i-have");
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	public void testInvalidWsInfoCausesFailure() throws Exception {
		testArea.hasFile("wsroot/as-example-developer/i-have/conf/ws-info",
				"invalid\n");
		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals("Please specify WSNAME in " + asTest
					+ "/i-have/conf/ws-info", e.getMessage());
		}
	}

	public void testMissingWsdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefCreation()
			throws Exception {
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/conf/ws-info",
				"WSNAME=example\n"
						+ "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");
		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals(
					"I created\n"
							+ asTest
							+ "/i-have/wsdefdef/src/main/java/com/example/wsdefdef/ExampleWsProvider.java\n"
							+ "and\n"
							+ asTest
							+ "/i-have/wsdef/src/main/java/com/example/wsdef/Workspace.java"
							+ "\nPlease edit them and rerun me.",
					e.getMessage());
		}
		String wsdefdefContent = testArea
				.contentOf("wsroot/as-example-developer/i-have/wsdefdef/src/main/java/com/example/wsdefdef/ExampleWsProvider.java");
		assertTrue(wsdefdefContent
				.startsWith("package com.example.wsdefdef;\n"));
		assertTrue(wsdefdefContent.contains(" class ExampleWsProvider "));
		// full content will be asserted by functionality
		String wsdefContent = testArea
				.contentOf("wsroot/as-example-developer/i-have/wsdef/src/main/java/com/example/wsdef/Workspace.java");
		assertTrue(wsdefContent.startsWith("package com.example.wsdef;\n"));
		assertTrue(wsdefContent.contains(" class Workspace "));
		// full content will be asserted by functionality
	}

	public void testIwant3AlsoCreatesWishScriptsForExampleWsDef()
			throws Exception {
		testMissingWsdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefCreation();
		// targets:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/target/hello/as-path")
				.startsWith("#!/bin/bash\n"));
		// side-effects:
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/list-of/side-effects")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/side-effect/eclipse-settings/effective")
				.startsWith("#!/bin/bash\n"));
	}

	public void testIwant3DoesNotLeaveOldWishScriptsWhenTargetWasRenamedAndThereAreNoSideEffectsAnyMore()
			throws Exception {
		testIwant3AlsoCreatesWishScriptsForExampleWsDef();

		String wsdefRelpath = "wsroot/as-example-developer/i-have/wsdef/src/main/java/com/example/wsdef/Workspace.java";
		String wsdefContent = testArea.contentOf(wsdefRelpath);
		// renamed target
		wsdefContent = wsdefContent.replace("new HelloTarget(\"hello\"",
				"new HelloTarget(\"renamed-hello\"");
		// no side-effects
		wsdefContent = wsdefContent
				.replace(
						"SideEffectDefinitionContext ctx) {",
						"SideEffectDefinitionContext ctx) { if(true) return java.util.Collections.emptyList(); else");
		testArea.hasFile(wsdefRelpath, wsdefContent);

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertTrue(e
					.getMessage()
					.contains(
							"Try "
									+ wsRoot
									+ "/as-example-developer/with/bash/iwant/list-of/targets"));
		}

		// the target wish script has been renamed:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/target/renamed-hello/as-path")
				.startsWith("#!/bin/bash\n"));
		assertFalse(new File(wsRoot,
				"as-example-developer/with/bash/iwant/target/hello").exists());

		// no side-effects so the whole side-effect directory has disappeared:
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/list-of/side-effects")
				.startsWith("#!/bin/bash\n"));
		assertFalse(new File(wsRoot,
				"as-example-developer/with/bash/iwant/side-effect").exists());

		// help.sh is still there:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/help.sh")
				.startsWith("#!/bin/bash\n"));
	}

	public void testIwant3DoesNotLeaveOldWishScriptsWhenSideEffectWasRenamedAndThereAreNoTargetsAnyMore()
			throws Exception {
		testIwant3AlsoCreatesWishScriptsForExampleWsDef();

		String wsdefRelpath = "wsroot/as-example-developer/i-have/wsdef/src/main/java/com/example/wsdef/Workspace.java";
		String wsdefContent = testArea.contentOf(wsdefRelpath);

		// no targets
		wsdefContent = wsdefContent
				.replace(" targets() {",
						" targets() { if(true) return java.util.Collections.emptyList(); else");
		// renamed side-effect
		wsdefContent = wsdefContent.replace(".name(\"eclipse-settings\")",
				".name(\"renamed-eclipse-settings\")");
		testArea.hasFile(wsdefRelpath, wsdefContent);

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertTrue(e
					.getMessage()
					.contains(
							"Try "
									+ wsRoot
									+ "/as-example-developer/with/bash/iwant/list-of/targets"));
		}

		// no targets so the whole target directory has disappeared:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertFalse(new File(wsRoot,
				"as-example-developer/with/bash/iwant/target").exists());

		// the side-effect wish script has been renamed:
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/list-of/side-effects")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/side-effect/renamed-eclipse-settings/effective")
				.startsWith("#!/bin/bash\n"));
		assertFalse(new File(wsRoot,
				"as-example-developer/with/bash/iwant/side-effect/eclipse-settings")
				.exists());

		// help.sh is still there:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/help.sh")
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

	public void testIwant3CreatesCombinedSources() throws Exception {
		testListOfSideEffectsOfExampleWsDef();

		assertTrue(combinedIwantSrc.exists());
		assertTrue(new File(combinedIwantSrc, "net/sf/iwant/entry2/Iwant2.java")
				.exists());
		assertTrue(new File(combinedIwantSrc,
				"net/sf/iwant/api/wsdef/MockedApiWsdef.java").exists());
	}

	private static String modifiedExampleWsDef() {
		StringBuilder wsdef = new StringBuilder();
		wsdef.append("package com.example.wsdef;\n");
		wsdef.append("\n");
		wsdef.append("import java.util.Arrays;\n");
		wsdef.append("import java.util.List;\n");
		wsdef.append("import net.sf.iwant.api.core.HelloTarget;\n");
		wsdef.append("import net.sf.iwant.api.model.SideEffect;\n");
		wsdef.append("import net.sf.iwant.api.model.Target;\n");
		wsdef.append("import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;\n");
		wsdef.append("import net.sf.iwant.api.wsdef.IwantWorkspace;\n");
		wsdef.append("import net.sf.iwant.eclipsesettings.EclipseSettings;\n");
		wsdef.append("\n");
		wsdef.append("public class ExampleWs implements IwantWorkspace {\n");
		wsdef.append("\n");
		wsdef.append("  @Override\n");
		wsdef.append("	public List<? extends Target> targets() {\n");
		wsdef.append("		return Arrays.asList(new HelloTarget(\"modified-hello\", \"content 1\"),\n");
		wsdef.append("			new HelloTarget(\"hello2\", \"content 2\"));\n");
		wsdef.append("	}\n");
		wsdef.append("\n");
		wsdef.append("	@Override\n");
		wsdef.append("	public List<? extends SideEffect> sideEffects(\n");
		wsdef.append("			SideEffectDefinitionContext ctx) {\n");
		wsdef.append("		return Arrays\n");
		wsdef.append("				.asList(EclipseSettings\n");
		wsdef.append("						.with()\n");
		wsdef.append("						.name(\"eclipse-settings\")\n");
		wsdef.append("						.modules(ctx.wsdefdefJavaModule(),\n");
		wsdef.append("								ctx.wsdefJavaModule()).end());\n");
		wsdef.append("	}\n");
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
		b.append("import net.sf.iwant.api.javamodules.JavaModule;\n");
		b.append("import net.sf.iwant.api.javamodules.JavaSrcModule;\n");
		b.append("import net.sf.iwant.api.model.Path;\n");
		b.append("import net.sf.iwant.api.model.Source;\n");
		b.append("import net.sf.iwant.api.wsdef.IwantWorkspaceProvider;\n");
		b.append("import net.sf.iwant.api.wsdef.WorkspaceDefinitionContext;\n");
		b.append("\n");
		b.append("public class ExampleWsProvider implements IwantWorkspaceProvider {\n");
		b.append("\n");
		b.append("      @Override\n");
		b.append("        public JavaSrcModule workspaceModule(WorkspaceDefinitionContext ctx) {\n");
		b.append("          return JavaSrcModule.with().name(\"example-workspace\")\n");
		b.append("            .locationUnderWsRoot(\"as-example-developer/i-have/wsdef\")\n");
		b.append("            .mainJava(\"src/main/java\").mainDeps(ctx.iwantApiModules()).end();\n");
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
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/conf/ws-info",
				"WSNAME=example\n"
						+ "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdefdef/src/main/java/"
						+ "com/example/wsdefdef/ExampleWsProvider.java",
				exampleWsProvider());
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWs.java",
				modifiedExampleWsDef());

		iwant3.evaluate(asTest, "list-of/targets");

		assertEquals("modified-hello\nhello2\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	/**
	 * This is the situation after checking out the workspace from
	 * version-control where the original author commited it after generating
	 * with the iwant wizard.
	 */
	public void testEmptyWishCreatesWishScriptsEvenWhenWsdefdefAndWsdefExist()
			throws Exception {
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/conf/ws-info",
				"WSNAME=example\n"
						+ "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdefdef/src/main/java/"
						+ "com/example/wsdefdef/ExampleWsProvider.java",
				exampleWsProvider());
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWs.java",
				modifiedExampleWsDef());

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals("(Using default user preferences (file " + asTest
					+ "/i-have/conf/user-preferences is missing):\n"
					+ "[workerCount=1])\n" + "Try " + asTest
					+ "/with/bash/iwant/list-of/targets", e.getMessage());
		}

		assertEquals("", out());

		assertTrue(new File(wsRoot,
				"as-example-developer/with/bash/iwant/list-of/targets")
				.exists());
		assertTrue(new File(wsRoot,
				"as-example-developer/with/bash/iwant/target/hello2/as-path")
				.exists());
		assertTrue(new File(wsRoot,
				"as-example-developer/with/bash/iwant/target/modified-hello/as-path")
				.exists());
	}

	public void testListOfTargetsOfModifiedWsDefAlsoCreatesWishScripts()
			throws Exception {
		testListOfTargetsOfModifiedWsDef();

		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/target/modified-hello/as-path")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/target/hello2/as-path")
				.startsWith("#!/bin/bash\n"));

		assertFalse(new File(wsRoot,
				"as-example-developer/with/bash/iwant/target/hello/as-path")
				.exists());
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

		// project names
		testArea.shallContainFragmentIn(
				"wsroot/as-example-developer/i-have/wsdefdef/.project",
				"<projectDescription>\n        <name>example-wsdefdef</name>");
		testArea.shallContainFragmentIn(
				"wsroot/as-example-developer/i-have/wsdef/.project",
				"<projectDescription>\n        <name>example-workspace</name>");

		// project source dirs
		testArea.shallContainFragmentIn(
				"wsroot/as-example-developer/i-have/wsdefdef/.classpath",
				"<classpathentry kind=\"src\" path=\"src/main/java\"/>");
		testArea.shallContainFragmentIn(
				"wsroot/as-example-developer/i-have/wsdef/.classpath",
				"<classpathentry kind=\"src\" path=\"src/main/java\"/>");

		// iwant classes in classpath cannot be tested by exact paths, they are
		// in different path when running this test inside eclipse than in a
		// build script

		// iwant sources in classpath
		testArea.shallContainFragmentIn(
				"wsroot/as-example-developer/i-have/wsdef/.classpath",
				" sourcepath=\"" + combinedIwantSrc + "\"/>");
		testArea.shallContainFragmentIn(
				"wsroot/as-example-developer/i-have/wsdefdef/.classpath",
				" sourcepath=\"" + combinedIwantSrc + "\"/>");

		// settings
		testArea.shallContainFragmentIn(
				"wsroot/as-example-developer/i-have/wsdef/"
						+ ".settings/org.eclipse.jdt.core.prefs",
				"org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6\n");
		testArea.shallContainFragmentIn(
				"wsroot/as-example-developer/i-have/wsdef/"
						+ ".settings/org.eclipse.jdt.ui.prefs",
				"formatter_profile=_iwant-generated\n");
	}

	public void testTargetModifiedHelloAsPathOfModifiedWsDef() throws Exception {
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/conf/ws-info",
				"WSNAME=example\n"
						+ "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdefdef/src/main/java/"
						+ "com/example/wsdefdef/ExampleWsProvider.java",
				exampleWsProvider());
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWs.java",
				modifiedExampleWsDef());

		iwant3.evaluate(asTest, "target/modified-hello/as-path");

		File cached = new File(asTest, ".i-cached/target/modified-hello");
		assertEquals(cached + "\n", out());
		assertEquals("", errIgnoringDebugLog());

		assertEquals("content 1", testArea.contentOf(cached));
	}

	public void testListOfTargetsFailsIfWsDefdefDoesNotCompile()
			throws Exception {
		testArea.hasFile("wsroot/as-example-developer/i-have/conf/ws-info",
				"WSNAME=example\n" + "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdef.ExampleWs\n");
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdefdef/src/main/java/com/example/wsdef/ExampleWs.java",
				"crap\n");

		try {
			iwant3.evaluate(asTest, "list-of/targets");
			fail();
		} catch (IwantException e) {
			assertEquals("Compilation failed.", e.getMessage());
		}
		assertEquals("", out());
		assertEquals(
				wsRoot
						+ "/as-example-developer/i-have/wsdefdef/src/main/java/com/example/wsdef/ExampleWs.java"
						+ ":1: error: reached end of file while parsing\n"
						+ "crap\n" + "^\n" + "1 error\n", errIgnoringDebugLog());
	}

	/**
	 * A learning test: the interface to cast to must come from the classloader
	 * of the running class, not by loading it again.
	 */
	public void testToLearnThatEvenTwoInstancesOfSameClassloaderLoadIncompatibleClasses()
			throws Exception {
		Class<?> exampleClass = Iwant3.class;
		File classes = new File(exampleClass.getResource(
				"/net/sf/iwant/entry3/Iwant3.class").toURI()).getParentFile()
				.getParentFile().getParentFile().getParentFile()
				.getParentFile();

		List<File> locations = Arrays.asList(classes);
		String className = exampleClass.getCanonicalName();
		Class<?> c1 = Iwant.classLoader(true, locations).loadClass(className);
		Class<?> c2 = Iwant.classLoader(true, locations).loadClass(className);
		assertEquals(className, c1.getCanonicalName());
		assertEquals(className, c2.getCanonicalName());
		assertFalse(c1 == c2);
		assertFalse(exampleClass.isAssignableFrom(c1));
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
			assertEquals("(Using default user preferences (file " + asTest
					+ "/i-have/conf/user-preferences is missing):\n"
					+ "[workerCount=1])\n" + "Try " + asTest
					+ "/with/bash/iwant/list-of/targets", e.getMessage());
		}

		assertEquals("", out());
	}

	/**
	 * Corresponds to calling help.sh once more
	 */
	public void testEmptyWishAfterCreationOfUserPreferencesFiles()
			throws Exception {
		testEmptyWishAfterCreationOfExampleWsDef();
		startOfOutAndErrCapture();

		Iwant.newTextFile(new File(asTest, "i-have/conf/user-preferences"),
				"workerCount=3");

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals("(Using user preferences from file " + asTest
					+ "/i-have/conf/user-preferences:\n" + "[workerCount=3])\n"
					+ "Try " + asTest + "/with/bash/iwant/list-of/targets",
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

		JavaClasses wsdDefClassesTarget = JavaClasses.with().name("wsdef")
				.srcDirs(Source.underWsroot("wsdef"))
				.classLocations(new TargetMock("iwant-api-classes")).end();

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

		JavaClasses wsdDefClassesTarget = JavaClasses
				.with()
				.name("wsdef")
				.srcDirs(Source.underWsroot("wsdef"))
				.classLocations(new TargetMock("iwant-api-classes"),
						new TargetMock("external-library")).end();

		File wsDefdefClasses = new File("wsDefdefClasses");
		File wsDefClasses = new File("wsDefClasses");
		List<File> cp = Iwant3.wsdefRuntimeClasspath(ctx, wsdDefClassesTarget,
				wsDefdefClasses, wsDefClasses);

		assertEquals("[wsDefdefClasses, wsDefClasses,"
				+ " cached/iwant-api-classes, cached/external-library]",
				cp.toString());
	}

	public void testListOfSideEffectsOfExampleWsDefWorksFromUnderSymbolicLinkOfWorkspace()
			throws Exception {
		testMissingWsdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefCreation();
		startOfOutAndErrCapture();

		File symlinkToWsRoot = new File(testArea.root(), "symlink-to-wsroot");
		Files.createSymbolicLink(symlinkToWsRoot.toPath(), wsRoot.toPath());

		iwant3.evaluate(new File(symlinkToWsRoot, "as-example-developer"),
				"list-of/side-effects");

		assertEquals("eclipse-settings\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

}
