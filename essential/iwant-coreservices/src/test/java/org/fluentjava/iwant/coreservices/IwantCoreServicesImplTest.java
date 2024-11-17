package org.fluentjava.iwant.coreservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IwantCoreServicesImplTest {

	private TestArea testArea;
	private Properties sysprops;
	private Map<String, String> env;
	private Iwant iwant;
	private IwantCoreServicesImpl services;

	@BeforeEach
	protected void before() throws Exception {
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

	@Test
	public void windowsBashExeIsNullOnLinux() {
		sysprops.put("os.name", "Linux");

		allWindowsBashesExist();

		assertNull(services.windowsBashExe());
	}

	@Test
	public void windosBashExeIsNullOnUnknownOs() {
		sysprops.put("os.name", "SomethingExotic");

		allWindowsBashesExist();

		assertNull(services.windowsBashExe());
	}

	@Test
	public void cygwin64ExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");
		File bashExe = existingCygwin64Bash();

		assertEquals(bashExe, services.windowsBashExe());
	}

	@Test
	public void cygwinExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");
		File bashExe = existingCygwinBash();

		assertEquals(bashExe, services.windowsBashExe());
	}

	@Test
	public void homeGitBashExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");

		File bashExe = existingHomeGitBash();

		assertEquals(bashExe, services.windowsBashExe());
	}

	@Test
	public void programFilesGitBashExeIsFoundOnWindows7() {
		sysprops.put("os.name", "Windows 7");

		File bashExe = existingProgramFilesGitBash();

		assertEquals(bashExe, services.windowsBashExe());
	}

	@Test
	public void gitBashDeducedFromEnvIsPreferredOverExistingBashes() {
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

	@Test
	public void envVariableShellIsIgnoredIfWeCannotDeduceItsNature() {
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

	@Test
	public void cygwin64IsPreferredOverCygwinAndGitBash() {
		sysprops.put("os.name", "Windows 7");
		File bash64Exe = existingCygwin64Bash();
		existingCygwinBash();
		existingHomeGitBash();
		existingProgramFilesGitBash();

		assertEquals(bash64Exe, services.windowsBashExe());
	}

	@Test
	public void cygwin64ExeIsFoundOnSomeUnknownFlavourOfWindows() {
		sysprops.put("os.name", "Windows the last one");
		File bashExe = existingCygwin64Bash();

		assertEquals(bashExe, services.windowsBashExe());
	}

	@Test
	public void missingBashIsAnErrorOnWindows7() {
		sysprops.put("os.name", "Windows 7");

		try {
			services.windowsBashExe();
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Cannot find cygwin (or git) bash.exe",
					e.getMessage());
		}
	}

	@Test
	public void nativeBashFormatIsTheOriginalOnNonWindows() {
		sysprops.put("os.name", "Anything but Windows");

		assertEquals("C:\\this\\is\\fine",
				services.toNativeBashFormat("C:\\this\\is\\fine"));
		assertEquals("C:/also/fine",
				services.toNativeBashFormat("C:/also/fine"));
	}

	@Test
	public void nativeBashFormatIsCygdriveFormatOnWindowsWithCygwin64() {
		sysprops.put("os.name", "Windows 7");
		existingCygwin64Bash();

		assertEquals("/cygdrive/c/some/path",
				services.toNativeBashFormat("C:\\some\\path"));
		assertEquals("/cygdrive/c/some/path",
				services.toNativeBashFormat("C:/some/path"));
	}

	@Test
	public void nativeBashFormatIsCygdriveFormatOnWindowsWithCygwin() {
		sysprops.put("os.name", "Windows 7");
		existingCygwinBash();

		assertEquals("/cygdrive/c/some/path",
				services.toNativeBashFormat("C:\\some\\path"));
		assertEquals("/cygdrive/c/some/path",
				services.toNativeBashFormat("C:/some/path"));
	}

	@Test
	public void nativeBashFormatIsSlashCFormatOnWindowsWithGitBash() {
		sysprops.put("os.name", "Windows 7");
		existingHomeGitBash();

		assertEquals("/c/some/path",
				services.toNativeBashFormat("C:\\some\\path"));
		assertEquals("/c/some/path",
				services.toNativeBashFormat("C:/some/path"));
	}

}
