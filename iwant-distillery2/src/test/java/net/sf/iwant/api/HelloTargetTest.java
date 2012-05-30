package net.sf.iwant.api;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.entry3.IwantEntry3TestArea;
import net.sf.iwant.io.StreamUtil;

public class HelloTargetTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private TargetEvaluationContext ctx;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		ctx = new TargetEvaluationContextMock(testArea);
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
		Target t = new HelloTarget("non-null", "hello content");

		File cachedContent = t.path(ctx);

		assertEquals("hello content", testArea.contentOf(cachedContent));
	}

}
