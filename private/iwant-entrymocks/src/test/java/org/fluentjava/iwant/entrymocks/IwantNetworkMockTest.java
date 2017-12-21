package org.fluentjava.iwant.entrymocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableUrl;
import org.fluentjava.iwant.testarea.TestArea;
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
			network.cacheOfContentFrom(
					new UnmodifiableUrl(Iwant.url("http://localhost")));
			fail();
		} catch (IllegalStateException e) {
			assertEquals("You forgot to teach UnmodifiableUrl:http://localhost",
					e.getMessage());
		}
	}

}
