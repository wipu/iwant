package net.sf.iwant.entrymocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import net.sf.iwant.testarea.TestArea;

import org.junit.Before;
import org.junit.Test;

public class IwantNetworkMockTest {

	private IwantNetworkMock network;

	@Before
	public void before() {
		TestArea testArea = TestArea.forTest(this);
		network = new IwantNetworkMock(testArea);
	}

	@Test
	public void getterGivesFriendlyExceptionIfNotTaught() {
		try {
			network.svnkitUrl();
			fail();
		} catch (IllegalStateException e) {
			assertEquals("You forgot to teach svnkitUrl", e.getMessage());
		}
	}

}
