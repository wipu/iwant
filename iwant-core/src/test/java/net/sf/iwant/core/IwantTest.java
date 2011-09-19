package net.sf.iwant.core;

import java.io.IOException;

public class IwantTest extends WorkspaceBuilderTestBase {

	public void testMissingAsSomebodyCausesFriendlyFailure() throws IOException {
		try {
			Iwant.main(new String[] { wsRoot() + "/as-x-developer" });
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Internal error: missing " + wsRoot()
					+ "/as-x-developer/i-have", e.getMessage());
		}
		assertEquals("", out());
		assertEquals("", err());
	}

	public void testMissingIHaveCausesFriendlyFailure() throws IOException {
		directoryExists("as-x-developer");
		try {
			Iwant.main(new String[] { wsRoot() + "/as-x-developer" });
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Internal error: missing " + wsRoot()
					+ "/as-x-developer/i-have", e.getMessage());
		}
		assertEquals("", out());
		assertEquals("", err());
	}

	public void testMissingIHaveWsInfoGetsCreated() throws IOException {
		directoryExists("as-x-developer/i-have");
		try {
			Iwant.main(new String[] { wsRoot() + "/as-x-developer" });
			fail();
		} catch (IllegalStateException e) {
			assertEquals("I created " + wsRoot()
					+ "/as-x-developer/i-have/ws-info.conf for you."
					+ " Please edit it and rerun me.", e.getMessage());
		}
		assertEquals("", out());
		assertEquals("", err());
		assertEquals("# paths are relative to this file's directory\n"
				+ "WSNAME=example\n" + "WSROOT=../..\n"
				+ "WSDEF_SRC=../i-have/wsdef\n"
				+ "WSDEF_CLASS=com.example.wsdef.Workspace\n",
				contentOf(wsRoot() + "/as-x-developer/i-have/ws-info.conf"));
	}

}
