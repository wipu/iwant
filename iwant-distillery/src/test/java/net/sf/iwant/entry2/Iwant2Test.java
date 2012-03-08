package net.sf.iwant.entry2;

import junit.framework.TestCase;
import net.sf.iwant.entry.IwantEntryTestArea;
import net.sf.iwant.entry.IwantNetworkMock;

public class Iwant2Test extends TestCase {

	private IwantEntryTestArea testArea;
	private IwantNetworkMock network;
	@SuppressWarnings("unused")
	private Iwant2 iwant2;

	public void setUp() {
		testArea = new IwantEntryTestArea();
		network = new IwantNetworkMock(testArea);
		iwant2 = Iwant2.using(network);
	}

	public void testNothingYet() {
	}

}
