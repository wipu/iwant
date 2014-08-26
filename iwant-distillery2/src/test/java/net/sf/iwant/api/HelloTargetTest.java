package net.sf.iwant.api;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.coreservices.StreamUtil;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class HelloTargetTest extends TestCase {

	private TestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetworkMock network;
	private Iwant iwant;
	private File cached;
	private CachesMock caches;
	private File wsRoot;

	@Override
	public void setUp() {
		testArea = TestArea.forTest(this);
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.newDir("wsroot");
		caches = new CachesMock(wsRoot);
		ctx = new TargetEvaluationContextMock(iwant, caches);
		cached = testArea.newDir("cached");
		caches.cachesModifiableTargetsAt(cached);
	}

	public void testNullMessageContent() throws Exception {
		Target t = new HelloTarget("null", null);
		try {
			t.content(ctx);
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	public void testNullMessageRefreshTo() throws Exception {
		Target t = new HelloTarget("null", null);
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
		Target target = new HelloTarget("non-null", "hello content");

		target.path(ctx);

		assertEquals("hello content", testArea.contentOf("cached/non-null"));
	}

}
