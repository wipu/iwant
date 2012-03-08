package net.sf.iwant.entry2;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import net.sf.iwant.entry.IwantEntryTestArea;
import net.sf.iwant.entry.IwantNetworkMock;

public class Iwant2Test extends TestCase {

	private IwantEntryTestArea testArea;
	private IwantNetworkMock network;
	private Iwant2 iwant2;
	private File asSomeone;

	public void setUp() {
		testArea = new IwantEntryTestArea();
		network = new IwantNetworkMock(testArea);
		iwant2 = Iwant2.using(network);
		asSomeone = new File(testArea.root(), "as-test");
	}

	private void evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation()
			throws IOException {
		try {
			iwant2.evaluate(asSomeone);
			fail();
		} catch (RuntimeException e) {
			assertEquals("I created " + asSomeone + "/i-have/ws-info\n"
					+ "Please edit it and rerun me.", e.getMessage());
		}
		assertEquals("# paths are relative to this file's directory\n"
				+ "WSNAME=example\n" + "WSROOT=../..\n" + "WSDEF_SRC=wsdef\n"
				+ "WSDEF_CLASS=com.example.wsdef.Workspace\n",
				testArea.contentOf("as-test/i-have/ws-info"));
	}

	public void testMissingAsSomeoneCausesFriendlyFailureAndExampleCreation()
			throws IOException {
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

	public void testMissingWsInfoCausesFriendlyFailureAndExampleCreation()
			throws IOException {
		testArea.newDir("as-test");
		evaluateAndExpectFriendlyFailureAndExampleWsInfoCreation();
	}

}
