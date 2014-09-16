package net.sf.iwant.api.core;

import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.coreservices.StreamUtil;

public class HelloTargetTest extends IwantTestCase {

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

		assertEquals("hello content", contentOfCached("non-null"));
	}

}
