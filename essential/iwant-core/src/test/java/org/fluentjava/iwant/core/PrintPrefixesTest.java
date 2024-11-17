package org.fluentjava.iwant.core;

import junit.framework.TestCase;

public class PrintPrefixesTest{

	@Test public void nullPrefix() {
		PrintPrefixes p = PrintPrefixes.fromPrefix(null);
		assertEquals("", p.prefix());
		assertEquals("", p.errPrefix());
		assertEquals("", p.outPrefix());
		assertEquals("1\n2\n", p.multiLineErr("1\n2\n"));
	}

	@Test public void emptyPrefix() {
		PrintPrefixes p = PrintPrefixes.fromPrefix("");
		assertEquals("", p.prefix());
		assertEquals("", p.errPrefix());
		assertEquals("", p.outPrefix());
		assertEquals("1\n2\n", p.multiLineErr("1\n2\n"));
	}

	@Test public void nonEmptyPrefix() {
		PrintPrefixes p = PrintPrefixes.fromPrefix(":iwant:");
		assertEquals(":iwant:", p.prefix());
		assertEquals(":iwant:err:", p.errPrefix());
		assertEquals(":iwant:out:", p.outPrefix());

		assertEquals(":iwant:err:1", p.multiLineErr("1"));
		assertEquals(":iwant:err:1\n", p.multiLineErr("1\n"));
		assertEquals(":iwant:err:1\n:iwant:err:2", p.multiLineErr("1\n2"));
		assertEquals(":iwant:err:1\n:iwant:err:2\n", p.multiLineErr("1\n2\n"));
	}

}
