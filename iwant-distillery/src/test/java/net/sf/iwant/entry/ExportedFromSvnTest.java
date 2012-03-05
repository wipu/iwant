package net.sf.iwant.entry;

import java.io.File;

import junit.framework.TestCase;

public class ExportedFromSvnTest extends TestCase {

	private File svnkitDir;

	public void setUp() {
		// here we assume download and unzip have been tested
		svnkitDir = Iwant.usingRealNetwork().unzippedSvnkit();
	}

	public void testTodo() {
		System.out.println(svnkitDir);
	}

}
