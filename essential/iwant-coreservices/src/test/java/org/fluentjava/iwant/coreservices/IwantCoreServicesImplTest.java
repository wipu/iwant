package org.fluentjava.iwant.coreservices;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.testarea.TestArea;

import junit.framework.TestCase;

public class IwantCoreServicesImplTest extends TestCase {

	private TestArea testArea;
	private Properties sysprops;
	private Map<String, String> env;
	private Iwant iwant;
	private IwantCoreServicesImpl services;

	@Override
	protected void setUp() throws Exception {
		iwant = null;
		testArea = TestArea.forTest(this);
		sysprops = new Properties();
		env = new HashMap<>();
		services = new IwantCoreServicesImpl(iwant, testArea.root(), sysprops,
				env);
		File home = testArea.newDir("home");
		sysprops.put("user.home", home.getAbsolutePath());
	}

	private File existingCygwin64Bash() {
		return testArea.hasFile("cygwin64/bin/bash.exe", "ignored");
	}

	private File existingCygwinBash() {
		return testArea.hasFile("cygwin/bin/bash.exe", "ignored");
	}

	private File existingHomeGitBash() {
		return testArea.hasFile("home/AppData/Local/Programs/Git/bin/bash.exe",
				"ignored");
	}

	private File existingProgramFilesGitBash() {
		return testArea.hasFile("Program Files/Git/bin/bash.exe", "ignored");
	}

	private void allWindowsBashesExist() {
		existingCygwin64Bash();
		existingCygwinBash();
		existingHomeGitBash();
		existingProgramFilesGitBash();
	}

	public void testWindowsBashExeIsNullOnLinux() {
		sysprops.put("os.name", "Linux");

		allWindowsBashesExist();

		assertNull(services.windowsBashExe());
	}

	public void testWindosBashExeIsNullOnUnknownOs() {
		sysprops.put("os.name", "SomethingExotic");

		allWindowsBashesExist();

		assertNull(services.windowsBashExe());
	}

	public void testCygwin64ExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");
		File bashExe = existingCygwin64Bash();

		assertEquals(bashExe, services.windowsBashExe());
	}

	public void testCygwinExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");
		File bashExe = existingCygwinBash();

		assertEquals(bashExe, services.windowsBashExe());
	}

	public void testHomeGitBashExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");

		File bashExe = existingHomeGitBash();

		assertEquals(bashExe, services.windowsBashExe());
	}

	public void testProgramFilesGitBashExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");

		File bashExe = existingProgramFilesGitBash();

		assertEquals(bashExe, services.windowsBashExe());
	}

	public void testGitBashDeducedFromEnvIsPreferredOverExistingBashes() {
		File customBash = testArea.hasFile("custom/gitbash/bin/bash.exe",
				"ignored");
		File customMingw = testArea.newDir("custom/gitbash/mingw");
		sysprops.put("os.name", "Windows 7");
		env.put("SHELL", customBash.getAbsolutePath());
		env.put("MINGW_PREFIX", customMingw.getAbsolutePath());

		allWindowsBashesExist();

		assertEquals(customBash, services.windowsBashExe());
		// path conversion is git bash -style:
		assertEquals("/c/a/path", services.toNativeBashFormat("C:/a/path"));
	}

	public void testEnvVariableShellIsIgnoredIfWeCannotDeduceItsNature() {
		File customBash = testArea.hasFile("custom/gitbash/bin/bash.exe",
				"ignored");
		sysprops.put("os.name", "Windows 7");
		env.put("SHELL", customBash.getAbsolutePath());
		// nothing else in env to help in deduction

		File bash64Exe = existingCygwin64Bash();
		existingCygwinBash();
		existingHomeGitBash();
		existingProgramFilesGitBash();

		assertEquals(bash64Exe, services.windowsBashExe());
		// path conversion is cygwin-style:
		assertEquals("/cygdrive/c/a/path",
				services.toNativeBashFormat("C:/a/path"));
	}

	public void testCygwin64IsPreferredOverCygwinAndGitBash() {
		sysprops.put("os.name", "Windows 7");
		File bash64Exe = existingCygwin64Bash();
		existingCygwinBash();
		existingHomeGitBash();
		existingProgramFilesGitBash();

		assertEquals(bash64Exe, services.windowsBashExe());
	}

	public void testCygwin64ExeIsFoundOnSomeUnknownFlavourOfWindows() {
		sysprops.put("os.name", "Windows the last one");
		File bashExe = existingCygwin64Bash();

		assertEquals(bashExe, services.windowsBashExe());
	}

	public void testMissingBashIsAnErrorOnWindows7() {
		sysprops.put("os.name", "Windows 7");

		try {
			services.windowsBashExe();
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
		existingHomeGitBash();

		assertEquals("/c/some/path",
				services.toNativeBashFormat("C:\\some\\path"));
		assertEquals("/c/some/path",
				services.toNativeBashFormat("C:/some/path"));
	}

}
