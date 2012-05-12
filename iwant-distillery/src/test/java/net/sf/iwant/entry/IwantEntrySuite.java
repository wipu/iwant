package net.sf.iwant.entry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantEntrySuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.entry");
		suite.addTestSuite(FilenameEscapingTest.class);
		suite.addTestSuite(LocationsTest.class);
		suite.addTestSuite(UnmodifiableSourceTest.class);
		suite.addTestSuite(DownloadingTest.class);
		suite.addTestSuite(UnzippingTest.class);
		suite.addTestSuite(UnzippedSvnkitTest.class);
		suite.addTestSuite(ExportedFromSvnTest.class);
		suite.addTestSuite(IwantTest.class);
		return suite;
	}

}
