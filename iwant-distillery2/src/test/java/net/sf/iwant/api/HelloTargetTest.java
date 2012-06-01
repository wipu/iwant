package net.sf.iwant.api;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.IwantNetworkMock;
import net.sf.iwant.entry3.IwantEntry3TestArea;
import net.sf.iwant.io.StreamUtil;

public class HelloTargetTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetworkMock network;
	private Iwant iwant;
	private File cached;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		ctx = new TargetEvaluationContextMock(iwant);
		cached = new File(testArea.root(), "cached");
	}

	public void testNullMessageContent() throws Exception {
		Target t = new HelloTarget("null", null);
		ctx.cachesAt(t, cached);
		try {
			t.content(ctx);
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	public void testNullMessageRefreshTo() throws Exception {
		Target t = new HelloTarget("null", null);
		ctx.cachesAt(t, cached);
		try {
			t.path(ctx);
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	public void testNonNullMessageContent() throws Exception {
		Target t = new HelloTarget("non-null", "hello content");
		assertEquals("hello content", StreamUtil.toString(t.content(ctx)));
	}

	public void testNonNullMessagePath() throws Exception {
		Target t = new HelloTarget("non-null", "hello content");
		ctx.cachesAt(t, cached);

		File cachedAgain = t.path(ctx);
		assertEquals(cached, cachedAgain);

		assertEquals("hello content", testArea.contentOf(cachedAgain));
	}

}