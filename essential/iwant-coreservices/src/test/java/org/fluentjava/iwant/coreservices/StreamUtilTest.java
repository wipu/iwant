package org.fluentjava.iwant.coreservices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;

public class StreamUtilTest {

	@Test
	public void emptyStreamToString() {
		assertEquals("",
				StreamUtil.toString(new ByteArrayInputStream(new byte[0])));
	}

	public void test2CharStreamToString() {
		assertEquals("ab",
				StreamUtil.toString(new ByteArrayInputStream("ab".getBytes())));
	}

}
