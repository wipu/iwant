package com.example.hello.first;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HelloMainTest {

	@Test
	public void greetingUsesGivenTarget() {
		assertEquals("Hello world", HelloMain.greetingTo("world"));
		assertEquals("Hello Finland", HelloMain.greetingTo("Finland"));
	}

}
