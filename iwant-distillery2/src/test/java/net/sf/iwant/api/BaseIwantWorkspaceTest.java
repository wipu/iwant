package net.sf.iwant.api;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

public class BaseIwantWorkspaceTest extends TestCase {

	private ByteArrayOutputStream out;

	@Override
	public void setUp() {
		out = new ByteArrayOutputStream();
	}

	private class Hello extends BaseIwantWorkspace {

		@Override
		public Collection<?> targets() {
			return Arrays.asList("hello");
		}

	}

	private class TwoHellos extends BaseIwantWorkspace {

		@Override
		public Collection<?> targets() {
			return Arrays.asList("hello1", "hello2");
		}

	}

	public void testIllegalWishFromHello() {
		IwantWorkspace hello = new Hello();
		try {
			hello.iwant("illegal/wish", out);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal wish: illegal/wish\nlegal targets:[hello]",
					e.getMessage());
		}
		assertEquals("", out.toString());
	}

	public void testListOfTargetsFromHello() {
		IwantWorkspace hello = new Hello();
		hello.iwant("list-of/targets", out);
		assertEquals("hello\n", out.toString());
	}

	public void testTargetHelloAsPathFromHello() {
		IwantWorkspace hello = new Hello();
		hello.iwant("target/hello/as-path", out);
		assertEquals("todo path to hello\n", out.toString());
	}

	public void testListOfTargetsFromTwoHellos() {
		IwantWorkspace hello = new TwoHellos();
		hello.iwant("list-of/targets", out);
		assertEquals("hello1\nhello2\n", out.toString());
	}

	public void testTargetHello1AsPathFromTwoHellos() {
		IwantWorkspace hello = new TwoHellos();
		hello.iwant("target/hello1/as-path", out);
		assertEquals("todo path to hello1\n", out.toString());
	}

	public void testTargetHello2AsPathFromTwoHellos() {
		IwantWorkspace hello = new TwoHellos();
		hello.iwant("target/hello2/as-path", out);
		assertEquals("todo path to hello2\n", out.toString());
	}

}
