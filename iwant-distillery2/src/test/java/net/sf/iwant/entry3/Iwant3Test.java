package net.sf.iwant.entry3;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.entry.IwantNetworkMock;
import net.sf.iwant.entry3.Iwant3.IwantException;

public class Iwant3Test extends TestCase {

	private IwantEntry3TestArea testArea;
	private IwantNetworkMock network;
	private Iwant3 iwant3;
	private File asSomeone;

	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant3 = Iwant3.using(network);
		asSomeone = new File(testArea.root(), "as-test");
	}

	private void evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation() {
		try {
			iwant3.evaluate(asSomeone);
			fail();
		} catch (IwantException e) {
			assertEquals("I created " + asSomeone + "/i-have/ws-info\n"
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
			iwant3.evaluate(asSomeone);
			fail();
		} catch (IwantException e) {
			assertEquals("Please specify WSNAME in " + asSomeone
					+ "/i-have/ws-info", e.getMessage());
		}
	}

	public void testMissingWsdefCausesFriendlyFailureAndExampleCreation() {
		testArea.hasFile("as-test/i-have/ws-info", "WSNAME=example\n"
				+ "WSROOT=../..\n" + "WSDEF_SRC=wsdef\n"
				+ "WSDEF_CLASS=com.example.wsdef.Workspace\n");
		try {
			iwant3.evaluate(asSomeone);
			fail();
		} catch (IwantException e) {
			assertEquals("I created " + asSomeone
					+ "/i-have/wsdef/com/example/wsdef/Workspace.java"
					+ "\nPlease edit it and rerun me.", e.getMessage());
		}
		assertTrue(testArea.contentOf(
				"as-test/i-have/wsdef/com/example/wsdef/Workspace.java")
				.startsWith("package com.example.wsdef;\n"));
		// full content will be asserted by functionality
	}

}
