package org.fluentjava.iwant.coreservices;

import java.io.File;
import java.util.Properties;

import org.fluentjava.iwant.api.model.IwantCoreServices;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.testarea.TestArea;

import junit.framework.TestCase;

public class IwantCoreServicesImplTest extends TestCase {

	private TestArea testArea;
	private Properties sysprops;
	private Iwant iwant;
	private IwantCoreServices services;

	@Override
	protected void setUp() throws Exception {
		iwant = null;
		testArea = TestArea.forTest(this);
		sysprops = new Properties();
		services = new IwantCoreServicesImpl(iwant, testArea.root(), sysprops);
	}

	public void testCygwinBashExeIsNullOnLinux() {
		sysprops.put("os.name", "Linux");

		assertNull(services.cygwinBashExe());
	}

	public void testCygwinBashExeIsNullOnUnknownOs() {
		sysprops.put("os.name", "SomethingExotic");

		assertNull(services.cygwinBashExe());
	}

	public void testCygwin64ExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");
		File bashExe = testArea.hasFile("cygwin64/bin/bash.exe", "ignored");

		assertEquals(bashExe, services.cygwinBashExe());
	}

	public void testCygwin64IsPreferredOverCygwin() {
		sysprops.put("os.name", "Windows 7");
		File bash64Exe = testArea.hasFile("cygwin64/bin/bash.exe", "ignored");
		testArea.hasFile("cygwin/bin/bash.exe", "ignored");

		assertEquals(bash64Exe, services.cygwinBashExe());
	}

	public void testCygwinExeIsFoundOnWindows7IfCygwin64DoesNotExist() {
		sysprops.put("os.name", "Windows 7");
		File bashExe = testArea.hasFile("cygwin/bin/bash.exe", "ignored");

		assertEquals(bashExe, services.cygwinBashExe());
	}

	public void testCygwin64ExeIsFoundOnSomeUnknownFlavourOfWindows() {
		sysprops.put("os.name", "Windows the last one");
		File bashExe = testArea.hasFile("cygwin64/bin/bash.exe", "ignored");

		assertEquals(bashExe, services.cygwinBashExe());
	}

	public void testMissingCygwinIsAnErrorOnWindows7() {
		sysprops.put("os.name", "Windows 7");

		try {
			services.cygwinBashExe();
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Cannot find cygwin bash.exe", e.getMessage());
		}
	}

}
