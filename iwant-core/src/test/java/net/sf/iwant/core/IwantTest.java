package net.sf.iwant.core;

import java.io.IOException;
import java.security.Permission;

public class IwantTest extends WorkspaceBuilderTestBase {

	private SecurityManager origSecman;

	@Override
	public void setUp() {
		super.setUp();
		origSecman = System.getSecurityManager();
		System.setSecurityManager(new ExitCatcher());
	}

	private static class ExitCalledException extends SecurityException {

		private final int status;

		public ExitCalledException(int status) {
			this.status = status;
		}

		public int status() {
			return status;
		}

	}

	private static class ExitCatcher extends SecurityManager {

		@Override
		public void checkPermission(Permission perm) {
			// everything allowed
		}

		@Override
		public void checkExit(int status) {
			throw new ExitCalledException(status);
		}

	}

	@Override
	public void tearDown() {
		System.setSecurityManager(origSecman);
		super.tearDown();
	}

	public void testMissingAsSomebodyIsAnInternalFailure() throws IOException {
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

	public void testMissingIHaveIsAnInternalFailure() throws IOException {
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

	public void testMissingIHaveWsInfoGetsCreatedThenBuildAborts()
			throws IOException {
		directoryExists("as-x-developer/i-have");
		try {
			Iwant.main(new String[] { wsRoot() + "/as-x-developer" });
			fail();
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
		}
		assertEquals("", out());
		assertEquals("I created " + wsRoot()
				+ "/as-x-developer/i-have/ws-info.conf for you."
				+ " Please edit it and rerun me.\n", err());

		assertEquals("# paths are relative to this file's directory\n"
				+ "WSNAME=example\n" + "WSROOT=../..\n"
				+ "WSDEF_SRC=../i-have/wsdef\n"
				+ "WSDEF_CLASS=com.example.wsdef.Workspace\n",
				contentOf(wsRoot() + "/as-x-developer/i-have/ws-info.conf"));
	}

}
