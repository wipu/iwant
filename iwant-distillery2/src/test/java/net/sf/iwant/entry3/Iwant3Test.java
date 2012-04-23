package net.sf.iwant.entry3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant3NetworkMock;

public class Iwant3Test extends TestCase {

	private static final String LINE_SEPARATOR_KEY = "line.separator";

	private IwantEntry3TestArea testArea;
	private Iwant3NetworkMock network;
	private Iwant3 iwant3;
	private File asTest;
	private SecurityManager origSecman;
	private InputStream originalIn;
	private PrintStream originalOut;
	private PrintStream originalErr;
	private ByteArrayOutputStream out;
	private ByteArrayOutputStream err;
	private String originalLineSeparator;

	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new Iwant3NetworkMock(testArea);
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

	@Override
	public void tearDown() {
		System.setSecurityManager(origSecman);
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setProperty(LINE_SEPARATOR_KEY, originalLineSeparator);
	}

	private void evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation() {
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

	public void testMissingAsSomeoneCausesFriendlyFailureAndExampleCreation() {
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	public void testMissingIHaveCausesFriendlyFailureAndExampleCreation() {
		testArea.newDir("as-test");
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	public void testMissingWsInfoCausesFriendlyFailureAndExampleCreation() {
		testArea.newDir("as-test/i-have");
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	public void testInvalidWsInfoCausesFailure() {
		testArea.hasFile("as-test/i-have/ws-info", "invalid\n");
		try {
			iwant3.evaluate(asTest);
			fail();
		} catch (IwantException e) {
			assertEquals("Please specify WSNAME in " + asTest
					+ "/i-have/ws-info", e.getMessage());
		}
	}

	public void testMissingWsdefCausesFriendlyFailureAndExampleCreation() {
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

	public void testIwant3AlsoCreatesWishScriptsForExampleWsDef() {
		testMissingWsdefCausesFriendlyFailureAndExampleCreation();
		assertTrue(testArea
				.contentOf("as-test/with/bash/iwant/list-of/targets")
				.startsWith("#!/bin/bash\n"));
		assertTrue(testArea.contentOf(
				"as-test/with/bash/iwant/target/hello/as-path").startsWith(
				"#!/bin/bash\n"));
	}

	public void testListOfTargetsOfExampleWsDef() {
		testMissingWsdefCausesFriendlyFailureAndExampleCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "list-of/targets");

		assertEquals("hello\n", out());
		assertEquals("", err());
	}

	public void testTargetHelloAsPathOfExampleWsDef() {
		testMissingWsdefCausesFriendlyFailureAndExampleCreation();
		startOfOutAndErrCapture();

		iwant3.evaluate(asTest, "target/hello/as-path");

		assertEquals("todo path to hello\n", out());
		assertEquals("", err());
	}

	public void testListOfTargetsFailsIfWsDefDoesNotCompile() {
		testMissingWsdefCausesFriendlyFailureAndExampleCreation();
		startOfOutAndErrCapture();
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
				+ "1 error\n", err());
	}

}
