package net.sf.iwant.coreservices;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import net.sf.iwant.api.model.IwantCoreServices;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testarea.TestArea;

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

	public void testSvnExportDelegatesToIwant() throws MalformedURLException {
		final List<URL> urlsPassed = new ArrayList<URL>();
		final List<File> filesPassed = new ArrayList<File>();
		iwant = new Iwant(null) {
			@Override
			public void svnExport(URL from, File to) {
				urlsPassed.add(from);
				filesPassed.add(to);
			}
		};
		services = new IwantCoreServicesImpl(iwant, testArea.root(), sysprops);

		services.svnExported(new URL("http://localhost/an-url"), new File(
				"an-file"));

		assertEquals("[http://localhost/an-url]", urlsPassed.toString());
		assertEquals("[an-file]", filesPassed.toString());
	}

}
