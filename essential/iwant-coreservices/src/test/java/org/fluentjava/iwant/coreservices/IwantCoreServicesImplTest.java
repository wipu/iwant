package org.fluentjava.iwant.coreservices;

import java.io.File;
import java.util.Properties;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.testarea.TestArea;

import junit.framework.TestCase;

public class IwantCoreServicesImplTest extends TestCase {

	private TestArea testArea;
	private Properties sysprops;
	private Iwant iwant;
	private IwantCoreServicesImpl services;

	@Override
	protected void setUp() throws Exception {
		iwant = null;
		testArea = TestArea.forTest(this);
		sysprops = new Properties();
		services = new IwantCoreServicesImpl(iwant, testArea.root(), sysprops);
		File home = testArea.newDir("home");
		sysprops.put("user.home", home.getAbsolutePath());
	}

	private File existingCygwin64Bash() {
		return testArea.hasFile("cygwin64/bin/bash.exe", "ignored");
	}

	private File existingCygwinBash() {
		return testArea.hasFile("cygwin/bin/bash.exe", "ignored");
	}

	private File existingGitBash() {
		return testArea.hasFile("home/AppData/Local/Programs/Git/bin/bash.exe",
				"ignored");
	}

	private void allWindowsBashesExist() {
		existingCygwin64Bash();
		existingCygwinBash();
		existingGitBash();
	}

	public void testCygwinBashExeIsNullOnLinux() {
		sysprops.put("os.name", "Linux");

		allWindowsBashesExist();

		assertNull(services.cygwinBashExe());
	}

	public void testCygwinBashExeIsNullOnUnknownOs() {
		sysprops.put("os.name", "SomethingExotic");

		allWindowsBashesExist();

		assertNull(services.cygwinBashExe());
	}

	public void testCygwin64ExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");
		File bashExe = existingCygwin64Bash();

		assertEquals(bashExe, services.cygwinBashExe());
	}

	public void testCygwinExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");
		File bashExe = existingCygwinBash();

		assertEquals(bashExe, services.cygwinBashExe());
	}

	public void testGitBashExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");

		File bashExe = existingGitBash();

		assertEquals(bashExe, services.cygwinBashExe());
	}

	public void testCygwin64IsPreferredOverCygwinAndGitBash() {
		sysprops.put("os.name", "Windows 7");
		File bash64Exe = existingCygwin64Bash();
		existingCygwinBash();
		existingGitBash();

		assertEquals(bash64Exe, services.cygwinBashExe());
	}

	public void testCygwin64ExeIsFoundOnSomeUnknownFlavourOfWindows() {
		sysprops.put("os.name", "Windows the last one");
		File bashExe = existingCygwin64Bash();

		assertEquals(bashExe, services.cygwinBashExe());
	}

	public void testMissingBashIsAnErrorOnWindows7() {
		sysprops.put("os.name", "Windows 7");

		try {
			services.cygwinBashExe();
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Cannot find cygwin (or git) bash.exe",
					e.getMessage());
		}
	}

	public void testNativeBashFormatIsTheOriginalOnNonWindows() {
		sysprops.put("os.name", "Anything but Windows");

		assertEquals("C:\\this\\is\\fine",
				services.toNativeBashFormat("C:\\this\\is\\fine"));
		assertEquals("C:/also/fine",
				services.toNativeBashFormat("C:/also/fine"));
	}

	public void testNativeBashFormatIsCygdriveFormatOnWindowsWithCygwin64() {
		sysprops.put("os.name", "Windows 7");
		existingCygwin64Bash();

		assertEquals("/cygdrive/c/some/path",
				services.toNativeBashFormat("C:\\some\\path"));
		assertEquals("/cygdrive/c/some/path",
				services.toNativeBashFormat("C:/some/path"));
	}

	public void testNativeBashFormatIsCygdriveFormatOnWindowsWithCygwin() {
		sysprops.put("os.name", "Windows 7");
		existingCygwinBash();

		assertEquals("/cygdrive/c/some/path",
				services.toNativeBashFormat("C:\\some\\path"));
		assertEquals("/cygdrive/c/some/path",
				services.toNativeBashFormat("C:/some/path"));
	}

	public void testNativeBashFormatIsSlashCFormatOnWindowsWithGitBash() {
		sysprops.put("os.name", "Windows 7");
		existingGitBash();

		assertEquals("/c/some/path",
				services.toNativeBashFormat("C:\\some\\path"));
		assertEquals("/c/some/path",
				services.toNativeBashFormat("C:/some/path"));
	}

}
