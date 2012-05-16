package net.sf.iwant.entry3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestCase;
import net.sf.iwant.api.IwantWorkspace;
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
				+ "WSNAME=example\n" + "WSROOT=../..\n" + "WSDEF_SRC=wsdef\n"
				+ "WSDEF_CLASS=com.example.wsdef.Workspace\n",
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

	public void testMissingWsdefCausesFriendlyFailureAndExampleCreation()
			throws Exception {
		testArea.hasFile("as-test/i-have/ws-info", "WSNAME=example\n"
				+ "WSROOT=../..\n" + "WSDEF_SRC=wsdef\n"
				+ "WSDEF_CLASS=com.example.wsdef.ExampleWs\n");
		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals("I created " + asTest
					+ "/i-have/wsdef/com/example/wsdef/ExampleWs.java"
					+ "\nPlease edit it and rerun me.", e.getMessage());
		}
		String javaContent = testArea
				.contentOf("as-test/i-have/wsdef/com/example/wsdef/ExampleWs.java");
		assertTrue(javaContent.startsWith("package com.example.wsdef;\n"));
		assertTrue(javaContent.contains(" class ExampleWs "));
		// full content will be asserted by functionality
	}

	public void testIwant3AlsoCreatesWishScriptsForExampleWsDef()
			throws Exception {
		testMissingWsdefCausesFriendlyFailureAndExampleCreation();
		assertTrue(testArea
				.contentOf("as-test/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"as-test/with/bash/iwant/target/hello/as-path").startsWith(
				"#!/bin/bash\n"));
	}

	public void testListOfTargetsOfExampleWsDef() throws Exception {
		Iwant.fileLog("jep");
		testMissingWsdefCausesFriendlyFailureAndExampleCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "list-of/targets");

		assertEquals("hello\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	private static String modifiedExampleWsDef() {
		StringBuilder wsdef = new StringBuilder();
		wsdef.append("package com.example.wsdef;\n");
		wsdef.append("\n");
		wsdef.append("import java.io.OutputStream;\n");
		wsdef.append("import net.sf.iwant.api.IwantWorkspace;\n");
		wsdef.append("\n");
		wsdef.append("public class ExampleWs implements IwantWorkspace {\n");
		wsdef.append("\n");
		wsdef.append("	public void iwant(String wish, OutputStream out) {\n");
		wsdef.append("		if (\"list-of/targets\".equals(wish)) {\n");
		wsdef.append("			System.out.println(\"modified-hello\");\n");
		wsdef.append("		} else {\n");
		wsdef.append("			System.out.println(\"todo path to modified-hello\");\n");
		wsdef.append("		}\n");
		wsdef.append("	}\n");
		wsdef.append("\n");
		wsdef.append("}\n");
		return wsdef.toString();
	}

	public void testListOfTargetsOfModifiedWsDef() throws Exception {
		testArea.hasFile("as-test/i-have/ws-info", "WSNAME=example\n"
				+ "WSROOT=../..\n" + "WSDEF_SRC=wsdef\n"
				+ "WSDEF_CLASS=com.example.wsdef.ExampleWs\n");
		testArea.hasFile(
				"as-test/i-have/wsdef/com/example/wsdef/ExampleWs.java",
				modifiedExampleWsDef());

		iwant3.evaluate(asTest, "list-of/targets");

		assertEquals("modified-hello\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	public void testTargetHelloAsPathOfExampleWsDef() throws Exception {
		testMissingWsdefCausesFriendlyFailureAndExampleCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "target/hello/as-path");

		assertEquals("todo path to hello\n", out());
		assertEquals("", errIgnoringDebugLog());
	}

	public void testTargetHelloAsPathOfModifiedWsDef() throws Exception {
		testArea.hasFile("as-test/i-have/ws-info", "WSNAME=example\n"
				+ "WSROOT=../..\n" + "WSDEF_SRC=wsdef\n"
				+ "WSDEF_CLASS=com.example.wsdef.ExampleWs\n");
		testArea.hasFile(
				"as-test/i-have/wsdef/com/example/wsdef/ExampleWs.java",
				modifiedExampleWsDef());

		iwant3.evaluate(asTest, "target/hello/as-path");

		assertEquals("todo path to modified-hello\n", out());
		assertEquals("", errIgnoringDebugLog());
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

		File[] locations = { classes };
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
		testMissingWsdefCausesFriendlyFailureAndExampleCreation();
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

}
