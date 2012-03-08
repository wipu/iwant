package net.sf.iwant;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.iwant.entry.IwantEntrySuite;
import net.sf.iwant.entry2.IwantEntry2Suite;

public class IwantDistillerySuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant");
		suite.addTest(IwantEntrySuite.suite());
		suite.addTest(IwantEntry2Suite.suite());
		return suite;
	}

}
