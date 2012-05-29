package net.sf.iwant.api;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.entry3.IwantEntry3TestArea;
import net.sf.iwant.io.StreamUtil;

public class HelloTargetTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private File cachedContent;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		cachedContent = new File(testArea.root(), "content");
	}

	public void testNullMessageContent() {
		Target t = new HelloTarget("null", null);
		try {
			t.content();
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	public void testNullMessageRefreshTo() throws Exception {
		Target t = new HelloTarget("null", null);
		try {
			t.refreshTo(cachedContent);
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	public void testNonNullMessageContent() {
		Target t = new HelloTarget("non-null", "hello content");
		assertEquals("hello content", StreamUtil.toString(t.content()));
	}

	public void testNonNullMessageRefreshTo() throws Exception {
		Target t = new HelloTarget("non-null", "hello content");

		t.refreshTo(cachedContent);

		assertEquals("hello content", testArea.contentOf(cachedContent));
	}

}
