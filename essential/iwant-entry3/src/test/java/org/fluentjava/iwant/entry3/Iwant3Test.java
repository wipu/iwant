package org.fluentjava.iwant.entry3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.core.ScriptGenerated;
import org.fluentjava.iwant.api.javamodules.JavaClasses;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.apimocks.CachesMock;
import org.fluentjava.iwant.apimocks.TargetEvaluationContextMock;
import org.fluentjava.iwant.apimocks.TargetMock;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableIwantBootstrapperClassesFromIwantWsRoot;
import org.fluentjava.iwant.entry3.Iwant3.CombinedSrcFromUnmodifiableIwantEssential;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.iwantwsrootfinder.IwantWsRootFinder;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Iwant3Test {

	private static final String LINE_SEPARATOR_KEY = "line.separator";
	private static final int NCPU = Runtime.getRuntime().availableProcessors();
	private TestArea testArea;
	private IwantNetworkMock network;
	private Iwant3 iwant3;
	private File wsRoot;
	private File asTest;
	private InputStream originalIn;
	private PrintStream originalOut;
	private PrintStream originalErr;
	private ByteArrayOutputStream out;
	private ByteArrayOutputStream err;
	private String originalLineSeparator;
	private File combinedIwantSrc;

	@BeforeEach
	public void before() throws Exception {
		testArea = TestArea.forTest(this);
		String asSomeone = "wsroot/as-example-developer";
		testArea.hasFile(asSomeone + "/with/bash/iwant/help.sh",
				"#!/bin/bash\njust a mock because this exists in real life\n");
		File iwantZip = mockWsRootZip();
		URL iwantFromUrl = Iwant.fileToUrl(iwantZip);
		testArea.hasFile(asSomeone + "/i-have/conf/iwant-from",
				"#just a mock because this exists in real life\n"
						+ "iwant-from=" + iwantFromUrl + "\n");
		network = new IwantNetworkMock(testArea);

		File cachedIwantZip = network.cachesUrlAt(iwantFromUrl,
				"cached-iwant.zip");
		File cachedIwantZipUnzipped = network.cachesZipAt(
				Iwant.fileToUrl(cachedIwantZip), "iwant.zip.unzipped");
		File cachedIwantEssential = new File(cachedIwantZipUnzipped,
				"iwant-mock-wsroot/essential");
		network.cachesAt(
				new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
						cachedIwantEssential),
				"iwant-bootstrapper-classes");
		combinedIwantSrc = network.cachesAt(
				new CombinedSrcFromUnmodifiableIwantEssential(
						cachedIwantEssential),
				"combined-iwant-essential-sources");

		Iwant.using(network).iwantSourceOfWishedVersion(
				new File(testArea.root(), asSomeone));

		iwant3 = Iwant3.using(network, cachedIwantEssential);
		wsRoot = new File(testArea.root(), "wsroot");
		asTest = new File(wsRoot, "as-example-developer");
		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
		originalLineSeparator = System.getProperty(LINE_SEPARATOR_KEY);
		System.setProperty(LINE_SEPARATOR_KEY, "\n");
		startOfOutAndErrCapture();
	}

	/**
	 * TODO reuse, this is redundant
	 */
	private File mockWsRootZip() {
		try {
			File wsRoot = IwantWsRootFinder.mockWsRoot();
			File zip = new File(testArea.root(), "mock-iwant-wsroot.zip");
			ScriptGenerated.execute(wsRoot.getParentFile(), Arrays.asList("zip",
					"-0", "-q", "-r", zip.getAbsolutePath(), wsRoot.getName()));
			return zip;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	@AfterEach
	public void after() {
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
		assertEquals("# paths are relative to this file's directory\n"
				+ "WSNAME=example\n" + "WSROOT=../../..\n"
				+ "WSDEFDEF_MODULE=../wsdefdef\n"
				+ "WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWorkspaceProvider\n",
				testArea.contentOf(
						"wsroot/as-example-developer/i-have/conf/ws-info"));
	}

	@Test
	public void missingAsSomeoneCausesFriendlyFailureAndExampleCreation()
			throws Exception {
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	@Test
	public void missingIHaveCausesFriendlyFailureAndExampleCreation()
			throws Exception {
		testArea.newDir("as-example-developer");
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	@Test
	public void missingWsInfoCausesFriendlyFailureAndExampleCreation()
			throws Exception {
		testArea.newDir("as-example-developer/i-have");
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	@Test
	public void invalidWsInfoCausesFailure() throws Exception {
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

	@Test
	public void missingWsdefdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefAndWsCreation()
			throws Exception {
		testArea.hasFile("wsroot/as-example-developer/i-have/conf/ws-info",
				"WSNAME=example\n" + "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");

		spendSomeTimeLikeARealUser();

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals("I created\n" + asTest
					+ "/i-have/wsdefdef/src/main/java/com/example/wsdefdef/ExampleWsProvider.java\n"
					+ "and\n" + asTest
					+ "/i-have/wsdef/src/main/java/com/example/wsdef/ExampleWorkspaceFactory.java\n"
					+ "and\n" + asTest
					+ "/i-have/wsdef/src/main/java/com/example/wsdef/ExampleWorkspace.java"
					+ "\nPlease edit them and rerun me.\n"
					+ "If you want to use Eclipse for editing, run " + asTest
					+ "/with/bash/iwant/side-effect/eclipse-settings/effective first.",
					e.getMessage());
		}
		String wsdefdefContent = testArea.contentOf(
				"wsroot/as-example-developer/i-have/wsdefdef/src/main/java/com/example/wsdefdef/ExampleWsProvider.java");
		assertTrue(
				wsdefdefContent.startsWith("package com.example.wsdefdef;\n"));
		assertTrue(wsdefdefContent.contains(
				" class ExampleWsProvider implements WorkspaceModuleProvider "));
		// full content will be asserted by functionality
		String wsdefContent = testArea.contentOf(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/com/example/wsdef/ExampleWorkspaceFactory.java");
		assertTrue(wsdefContent.startsWith("package com.example.wsdef;\n"));
		assertTrue(wsdefContent.contains(
				" class ExampleWorkspaceFactory implements WorkspaceFactory "));
		// full content will be asserted by functionality
		String wsContent = testArea.contentOf(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/com/example/wsdef/ExampleWorkspace.java");
		assertTrue(wsContent.startsWith("package com.example.wsdef;\n"));
		assertTrue(wsContent
				.contains(" class ExampleWorkspace implements Workspace"));
		// full content will be asserted by functionality
	}

	/**
	 * Act slowly like a normal user so the ingredients will be strictly older
	 * than files derived from them.
	 */
	private static void spendSomeTimeLikeARealUser()
			throws InterruptedException {
		// act slowly like a normal user so the ingredients will be strictly
		// older than files derived from them:
		Thread.sleep(1000L);
	}

	@Test
	public void iwant3AlsoCreatesWishScriptsForExampleWsDef() throws Exception {
		missingWsdefdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefAndWsCreation();
		// targets:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/target/hello/as-path")
				.startsWith("#!/bin/bash\n"));
		// side-effects:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/side-effects")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/side-effect/eclipse-settings/effective")
				.startsWith("#!/bin/bash\n"));
	}

	@Test
	public void iwant3DoesNotLeaveOldWishScriptsWhenTargetWasRenamedAndThereAreNoSideEffectsAnyMore()
			throws Exception {
		iwant3AlsoCreatesWishScriptsForExampleWsDef();

		String wsdefRelpath = "wsroot/as-example-developer/i-have/wsdef/src/main/java/com/example/wsdef/ExampleWorkspace.java";
		String wsdefContent = testArea.contentOf(wsdefRelpath);
		// renamed target
		wsdefContent = wsdefContent.replace("new HelloTarget(\"hello\"",
				"new HelloTarget(\"renamed-hello\"");
		// no side-effects
		wsdefContent = wsdefContent.replace(
				"SideEffectDefinitionContext ctx) {",
				"SideEffectDefinitionContext ctx) { if(true) return java.util.Collections.emptyList(); else");
		testArea.hasFile(wsdefRelpath, wsdefContent);

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertTrue(e.getMessage().contains("Try " + wsRoot
					+ "/as-example-developer/with/bash/iwant/list-of/side-effects"));
		}

		// the target wish script has been renamed:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/target/renamed-hello/as-path")
				.startsWith("#!/bin/bash\n"));
		assertFalse(new File(wsRoot,
				"as-example-developer/with/bash/iwant/target/hello").exists());

		// no side-effects so the whole side-effect directory has disappeared:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/side-effects")
				.startsWith("#!/bin/bash\n"));
		assertFalse(new File(wsRoot,
				"as-example-developer/with/bash/iwant/side-effect").exists());

		// help.sh is still there:
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/help.sh")
				.startsWith("#!/bin/bash\n"));
	}

	@Test
	public void iwant3DoesNotLeaveOldWishScriptsWhenSideEffectWasRenamedAndThereAreNoTargetsAnyMore()
			throws Exception {
		iwant3AlsoCreatesWishScriptsForExampleWsDef();

		String wsdefRelpath = "wsroot/as-example-developer/i-have/wsdef/src/main/java/com/example/wsdef/ExampleWorkspace.java";
		String wsdefContent = testArea.contentOf(wsdefRelpath);

		// no targets
		wsdefContent = wsdefContent.replace(
				" targets(TargetDefinitionContext ctx) {",
				" targets(TargetDefinitionContext ctx) { if(true) return java.util.Collections.emptyList(); else");
		// renamed side-effect
		wsdefContent = wsdefContent.replace(".name(\"eclipse-settings\")",
				".name(\"renamed-eclipse-settings\")");
		testArea.hasFile(wsdefRelpath, wsdefContent);

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertTrue(e.getMessage().contains("Try " + wsRoot
					+ "/as-example-developer/with/bash/iwant/list-of/side-effects"));
		}

		// no targets so the whole target directory has disappeared:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertFalse(
				new File(wsRoot, "as-example-developer/with/bash/iwant/target")
						.exists());

		// the side-effect wish script has been renamed:
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/side-effects")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/side-effect/renamed-eclipse-settings/effective")
				.startsWith("#!/bin/bash\n"));
		assertFalse(new File(wsRoot,
				"as-example-developer/with/bash/iwant/side-effect/eclipse-settings")
						.exists());

		// help.sh is still there:
		assertTrue(testArea
				.contentOf(
						"wsroot/as-example-developer/with/bash/iwant/help.sh")
				.startsWith("#!/bin/bash\n"));
	}

	@Test
	public void listOfTargetsOfExampleWsDef() throws Exception {
		missingWsdefdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefAndWsCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "list-of/targets");

		assertEquals("hello\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	@Test
	public void listOfSideEffectsOfExampleWsDef() throws Exception {
		missingWsdefdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefAndWsCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "list-of/side-effects");

		assertEquals("eclipse-settings\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	@Test
	public void iwant3CreatesCombinedSources() throws Exception {
		listOfSideEffectsOfExampleWsDef();

		assertTrue(combinedIwantSrc.exists());
		assertTrue(new File(combinedIwantSrc,
				"org/fluentjava/iwant/entry2/Iwant2.java").exists());
		assertTrue(new File(combinedIwantSrc,
				"org/fluentjava/iwant/api/wsdef/MockedApiWsdef.java").exists());
	}

	private static String modifiedExampleWs() {
		StringBuilder wsdef = new StringBuilder();
		wsdef.append("package com.example.wsdef;\n");
		wsdef.append("\n");
		wsdef.append("import java.util.Arrays;\n");
		wsdef.append("import java.util.List;\n");
		wsdef.append("import org.fluentjava.iwant.api.core.HelloTarget;\n");
		wsdef.append("import org.fluentjava.iwant.api.model.SideEffect;\n");
		wsdef.append("import org.fluentjava.iwant.api.model.Target;\n");
		wsdef.append(
				"import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;\n");
		wsdef.append(
				"import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;\n");
		wsdef.append("import org.fluentjava.iwant.api.wsdef.Workspace;\n");
		wsdef.append(
				"import org.fluentjava.iwant.eclipsesettings.EclipseSettings;\n");
		wsdef.append("\n");
		wsdef.append("public class ExampleWs implements Workspace {\n");
		wsdef.append("\n");
		wsdef.append("  @Override\n");
		wsdef.append(
				"	public List<? extends Target> targets(TargetDefinitionContext ctx) {\n");
		wsdef.append(
				"		return Arrays.asList(new HelloTarget(\"modified-hello\", \"content 1\"),\n");
		wsdef.append(
				"			new HelloTarget(\"hello2\", \"content 2\"));\n");
		wsdef.append("	}\n");
		wsdef.append("\n");
		wsdef.append("	@Override\n");
		wsdef.append("	public List<? extends SideEffect> sideEffects(\n");
		wsdef.append("			SideEffectDefinitionContext ctx) {\n");
		wsdef.append("		return Arrays\n");
		wsdef.append("				.asList(EclipseSettings\n");
		wsdef.append("						.with()\n");
		wsdef.append("						.name(\"eclipse-settings\")\n");
		wsdef.append(
				"						.modules(ctx.wsdefdefJavaModule(),\n");
		wsdef.append(
				"								ctx.wsdefJavaModule()).end());\n");
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
		b.append("import org.fluentjava.iwant.api.javamodules.JavaModule;\n");
		b.append(
				"import org.fluentjava.iwant.api.javamodules.JavaSrcModule;\n");
		b.append("import org.fluentjava.iwant.api.model.Path;\n");
		b.append("import org.fluentjava.iwant.api.model.Source;\n");
		b.append(
				"import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;\n");
		b.append(
				"import org.fluentjava.iwant.api.wsdef.WorkspaceModuleProvider;\n");
		b.append("\n");
		b.append(
				"public class ExampleWsProvider implements WorkspaceModuleProvider {\n");
		b.append("\n");
		b.append("      @Override\n");
		b.append(
				"        public JavaSrcModule workspaceModule(WorkspaceModuleContext ctx) {\n");
		b.append(
				"          return JavaSrcModule.with().name(\"example-workspace\")\n");
		b.append(
				"            .locationUnderWsRoot(\"as-example-developer/i-have/wsdef\")\n");
		b.append(
				"            .mainJava(\"src/main/java\").mainDeps(ctx.iwantApiModules()).end();\n");
		b.append("      }\n");
		b.append("\n");
		b.append("      @Override\n");
		b.append("      public String workspaceFactoryClassname() {\n");
		b.append(
				"              return \"com.example.wsdef.ExampleWsFactory\";\n");
		b.append("      }\n");
		b.append("\n");
		b.append("}\n");
		return b.toString();
	}

	private static String exampleWsFactory() {
		StringBuilder b = new StringBuilder();
		b.append("package com.example.wsdef;\n");
		b.append("\n");
		b.append("import org.fluentjava.iwant.api.wsdef.Workspace;\n");
		b.append("import org.fluentjava.iwant.api.wsdef.WorkspaceContext;\n");
		b.append("import org.fluentjava.iwant.api.wsdef.WorkspaceFactory;\n");
		b.append("\n");
		b.append(
				"public class ExampleWsFactory implements WorkspaceFactory {\n");
		b.append("\n");
		b.append("	@Override\n");
		b.append("	public Workspace workspace(WorkspaceContext ctx) {\n");
		b.append("		return new ExampleWs();\n");
		b.append("	}\n");
		b.append("\n");
		b.append("}\n");
		return b.toString();
	}

	@Test
	public void listOfTargetsOfModifiedWsDef() throws Exception {
		testArea.hasFile("wsroot/as-example-developer/i-have/conf/ws-info",
				"WSNAME=example\n" + "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdefdef/src/main/java/"
						+ "com/example/wsdefdef/ExampleWsProvider.java",
				exampleWsProvider());
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWsFactory.java",
				exampleWsFactory());
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWs.java",
				modifiedExampleWs());

		iwant3.evaluate(asTest, "list-of/targets");

		assertEquals("modified-hello\nhello2\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	/**
	 * This is the situation after checking out the workspace from
	 * version-control where the original author commited it after generating
	 * with the iwant wizard.
	 */
	@Test
	public void emptyWishCreatesWishScriptsEvenWhenWsdefdefAndWsdefExist()
			throws Exception {
		testArea.hasFile("wsroot/as-example-developer/i-have/conf/ws-info",
				"WSNAME=example\n" + "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdefdef/src/main/java/"
						+ "com/example/wsdefdef/ExampleWsProvider.java",
				exampleWsProvider());
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWsFactory.java",
				exampleWsFactory());
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWs.java",
				modifiedExampleWs());

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals(
					"(Using default user preferences (file " + asTest
							+ "/i-have/conf/user-preferences is missing):\n"
							+ "[workerCount=" + NCPU + "])\n" + "Try " + asTest
							+ "/with/bash/iwant/list-of/side-effects\nor\n"
							+ asTest + "/with/bash/iwant/list-of/targets",
					e.getMessage());
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

	@Test
	public void listOfTargetsOfModifiedWsDefAlsoCreatesWishScripts()
			throws Exception {
		listOfTargetsOfModifiedWsDef();

		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/target/modified-hello/as-path")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"wsroot/as-example-developer/with/bash/iwant/target/hello2/as-path")
				.startsWith("#!/bin/bash\n"));

		assertFalse(new File(wsRoot,
				"as-example-developer/with/bash/iwant/target/hello/as-path")
						.exists());
	}

	@Test
	public void targetHelloAsPathOfExampleWsDef() throws Exception {
		missingWsdefdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefAndWsCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "target/hello/as-path");

		File cached = new File(asTest, ".i-cached/target/hello");
		assertEquals(cached + "\n", out());
		assertEquals("", errIgnoringDebugLog());

		assertEquals("hello from iwant\n", testArea.contentOf(cached));
	}

	@Test
	public void sideEffectEclipseSettingsEffectiveOfExampleWsDef()
			throws Exception {
		missingWsdefdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefAndWsCreation();
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
				"<projectDescription>\n        <name>example-wsdef</name>");

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
				"org.eclipse.jdt.core.compiler.codegen.targetPlatform=11\n");
		testArea.shallContainFragmentIn(
				"wsroot/as-example-developer/i-have/wsdef/"
						+ ".settings/org.eclipse.jdt.ui.prefs",
				"formatter_profile=_iwant-generated\n");
	}

	@Test
	public void targetModifiedHelloAsPathOfModifiedWsDef() throws Exception {
		testArea.hasFile("wsroot/as-example-developer/i-have/conf/ws-info",
				"WSNAME=example\n" + "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWsProvider\n");
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdefdef/src/main/java/"
						+ "com/example/wsdefdef/ExampleWsProvider.java",
				exampleWsProvider());
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWsFactory.java",
				exampleWsFactory());
		testArea.hasFile(
				"wsroot/as-example-developer/i-have/wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWs.java",
				modifiedExampleWs());

		iwant3.evaluate(asTest, "target/modified-hello/as-path");

		File cached = new File(asTest, ".i-cached/target/modified-hello");
		assertEquals(cached + "\n", out());
		assertEquals("", errIgnoringDebugLog());

		assertEquals("content 1", testArea.contentOf(cached));
	}

	@Test
	public void listOfTargetsFailsIfWsDefdefDoesNotCompile() throws Exception {
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
		assertEquals(wsRoot
				+ "/as-example-developer/i-have/wsdefdef/src/main/java/com/example/wsdef/ExampleWs.java"
				+ ":1: error: reached end of file while parsing\n" + "crap\n"
				+ "^\n" + "1 error\n", errIgnoringDebugLog());
	}

	/**
	 * A learning test: the interface to cast to must come from the classloader
	 * of the running class, not by loading it again.
	 */
	@Test
	public void toLearnThatEvenTwoInstancesOfSameClassloaderLoadIncompatibleClasses()
			throws Exception {
		Class<?> exampleClass = Iwant3.class;
		File classes = new File(exampleClass
				.getResource("/org/fluentjava/iwant/entry3/Iwant3.class")
				.toURI()).getParentFile().getParentFile().getParentFile()
						.getParentFile().getParentFile();

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
	@Test
	public void emptyWishAfterCreationOfExampleWsDef() throws Exception {
		missingWsdefdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefAndWsCreation();
		startOfOutAndErrCapture();

		spendSomeTimeLikeARealUser();

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals(
					"(Using default user preferences (file " + asTest
							+ "/i-have/conf/user-preferences is missing):\n"
							+ "[workerCount=" + NCPU + "])\n" + "Try " + asTest
							+ "/with/bash/iwant/list-of/side-effects\nor\n"
							+ asTest + "/with/bash/iwant/list-of/targets",
					e.getMessage());
		}

		assertEquals("", out());
	}

	/**
	 * Corresponds to calling help.sh once more
	 */
	@Test
	public void emptyWishAfterCreationOfUserPreferencesFiles()
			throws Exception {
		emptyWishAfterCreationOfExampleWsDef();
		startOfOutAndErrCapture();

		Iwant.newTextFile(new File(asTest, "i-have/conf/user-preferences"),
				"workerCount=3");

		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals(
					"(Using user preferences from file " + asTest
							+ "/i-have/conf/user-preferences:\n"
							+ "[workerCount=3])\n" + "Try " + asTest
							+ "/with/bash/iwant/list-of/side-effects\nor\n"
							+ asTest + "/with/bash/iwant/list-of/targets",
					e.getMessage());
		}

		assertEquals("", out());
	}

	/**
	 * TODO extract class for this functionality and move these to its test, the
	 * setup is very different from other tests here.
	 */
	@Test
	public void wsdefRuntimeClasspathWhenWsdefClassesTargetDefinesNoExtra() {
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

	@Test
	public void wsdefRuntimeClasspathWhenWsdefClassesTargetDefinesAnExternalLibrary() {
		File wsRoot = testArea.newDir("wsroot");
		CachesMock caches = new CachesMock(wsRoot);
		TargetEvaluationContextMock ctx = new TargetEvaluationContextMock(
				Iwant.using(network), caches);
		caches.cachesModifiableTargetsAt(new File("cached"));

		JavaClasses wsdDefClassesTarget = JavaClasses.with().name("wsdef")
				.srcDirs(Source.underWsroot("wsdef"))
				.classLocations(new TargetMock("iwant-api-classes"),
						new TargetMock("external-library"))
				.end();

		File wsDefdefClasses = new File("wsDefdefClasses");
		File wsDefClasses = new File("wsDefClasses");
		List<File> cp = Iwant3.wsdefRuntimeClasspath(ctx, wsdDefClassesTarget,
				wsDefdefClasses, wsDefClasses);

		assertEquals(
				"[wsDefdefClasses, wsDefClasses,"
						+ " cached/iwant-api-classes, cached/external-library]",
				cp.toString());
	}

	@Test
	public void listOfSideEffectsOfExampleWsDefWorksFromUnderSymbolicLinkOfWorkspace()
			throws Exception {
		missingWsdefdefCausesFriendlyFailureAndExampleWsdefdefAndWsdefAndWsCreation();
		startOfOutAndErrCapture();

		File symlinkToWsRoot = new File(testArea.root(), "symlink-to-wsroot");
		Files.createSymbolicLink(symlinkToWsRoot.toPath(), wsRoot.toPath());

		iwant3.evaluate(new File(symlinkToWsRoot, "as-example-developer"),
				"list-of/side-effects");

		assertEquals("eclipse-settings\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	@Test
	public void usersWsClassesAreNotRecompiledIfNoIngredientHasChanged()
			throws Exception {
		Iwant.fileLog(
				"Starting testUsersWsClassesAreNotRecompiledIfNoIngredientHasChanged");
		emptyWishAfterCreationOfExampleWsDef();

		File wsdefdefClasses = new File(asTest, ".i-cached/wsdefdef-classes");
		long t1 = wsdefdefClasses.lastModified();

		iwant3.evaluate(asTest, "list-of/targets");

		long t2 = wsdefdefClasses.lastModified();

		assertEquals(t1, t2);
	}

}
