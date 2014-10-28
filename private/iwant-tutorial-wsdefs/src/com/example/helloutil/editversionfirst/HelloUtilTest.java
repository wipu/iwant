package com.example.helloutil.editversionfirst;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HelloUtilTest {

	@Test
	public void spaceSeparatedStrings() {
		assertEquals("Hello world",
				HelloUtil.spaceSeparatedWords("Hello", "world"));
		assertEquals("w0 w1", HelloUtil.spaceSeparatedWords("w0", "w1"));
	}

}
