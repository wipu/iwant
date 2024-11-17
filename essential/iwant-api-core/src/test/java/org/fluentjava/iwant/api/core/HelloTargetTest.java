package org.fluentjava.iwant.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.coreservices.StreamUtil;
import org.junit.jupiter.api.Test;

public class HelloTargetTest extends IwantTestCase {

	@Test
	public void nullMessageContent() throws Exception {
		Target t = new HelloTarget("null", null);
		try {
			t.content(ctx);
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	@Test
	public void nullMessageRefreshTo() throws Exception {
		Target t = new HelloTarget("null", null);
		try {
			t.path(ctx);
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	@Test
	public void nonNullMessageContent() throws Exception {
		Target t = new HelloTarget("non-null", "hello content");
		assertEquals("hello content", StreamUtil.toString(t.content(ctx)));
	}

	@Test
	public void nonNullMessagePath() throws Exception {
		Target target = new HelloTarget("non-null", "hello content");

		target.path(ctx);

		assertEquals("hello content", contentOfCached(target));
	}

}
