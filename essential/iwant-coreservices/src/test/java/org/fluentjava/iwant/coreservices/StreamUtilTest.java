package org.fluentjava.iwant.coreservices;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class StreamUtilTest extends TestCase {

	public void testEmptyStreamToString() {
		assertEquals("",
				StreamUtil.toString(new ByteArrayInputStream(new byte[0])));
	}

	public void test2CharStreamToString() {
		assertEquals("ab",
				StreamUtil.toString(new ByteArrayInputStream("ab".getBytes())));
	}

}
