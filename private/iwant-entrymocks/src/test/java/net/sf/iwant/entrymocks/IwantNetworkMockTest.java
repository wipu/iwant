package net.sf.iwant.entrymocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.testarea.TestArea;

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
			network.cacheLocation(
					new UnmodifiableUrl(Iwant.url("http://localhost")));
			fail();
		} catch (IllegalStateException e) {
			assertEquals("You forgot to teach UnmodifiableUrl:http://localhost",
					e.getMessage());
		}
	}

}
