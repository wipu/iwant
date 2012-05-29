package net.sf.iwant.entry3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import net.sf.iwant.api.HelloTarget;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.Target;

public class WishEvaluatorTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private ByteArrayOutputStream out;
	private WishEvaluator evaluator;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		out = new ByteArrayOutputStream();
		evaluator = new WishEvaluator(out, testArea.root());
	}

	private class Hello implements IwantWorkspace {

		@Override
		public List<? extends Target> targets() {
			return Arrays.asList(new HelloTarget("hello", "hello content"));
		}

	}

	private class TwoHellos implements IwantWorkspace {

		@Override
		public List<? extends Target> targets() {
			return Arrays.asList(new HelloTarget("hello1", "content 1"),
					new HelloTarget("hello2", "content 2"));
		}

	}

	public void testIllegalWishFromHello() {
		IwantWorkspace hello = new Hello();
		try {
			evaluator.iwant("illegal/wish", hello);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal wish: illegal/wish\nlegal targets:[hello]",
					e.getMessage());
		}
		assertEquals("", out.toString());
	}

	public void testListOfTargetsFromHello() {
		IwantWorkspace hello = new Hello();
		evaluator.iwant("list-of/targets", hello);
		assertEquals("hello\n", out.toString());
	}

	public void testTargetHelloAsPathFromHello() {
		IwantWorkspace hello = new Hello();

		evaluator.iwant("target/hello/as-path", hello);

		File cached = new File(testArea.root(),
				".todo-cached/target/hello/content");
		assertEquals(cached + "\n", out.toString());
		assertEquals("hello content", testArea.contentOf(cached));
	}

	public void testTargetHelloContentFromHello() {
		IwantWorkspace hello = new Hello();
		evaluator.iwant("target/hello/content", hello);
		assertEquals("hello content", out.toString());
	}

	public void testListOfTargetsFromTwoHellos() {
		IwantWorkspace hellos = new TwoHellos();
		evaluator.iwant("list-of/targets", hellos);
		assertEquals("hello1\nhello2\n", out.toString());
	}

	public void testTargetHello1AsPathFromTwoHellos() {
		IwantWorkspace hellos = new TwoHellos();

		evaluator.iwant("target/hello1/as-path", hellos);

		File cached = new File(testArea.root(),
				".todo-cached/target/hello1/content");
		assertEquals(cached + "\n", out.toString());
		assertEquals("content 1", testArea.contentOf(cached));
	}

	public void testTargetHello1ContentFromTwoHellos() {
		IwantWorkspace hellos = new TwoHellos();
		evaluator.iwant("target/hello1/content", hellos);
		assertEquals("content 1", out.toString());
	}

	public void testTargetHello2AsPathFromTwoHellos() {
		IwantWorkspace hellos = new TwoHellos();

		evaluator.iwant("target/hello2/as-path", hellos);

		File cached = new File(testArea.root(),
				".todo-cached/target/hello2/content");
		assertEquals(cached + "\n", out.toString());
		assertEquals("content 2", testArea.contentOf(cached));
	}

	public void testTargetHello2ContentFromTwoHellos() {
		IwantWorkspace hellos = new TwoHellos();
		evaluator.iwant("target/hello2/content", hellos);
		assertEquals("content 2", out.toString());
	}

	// target without a wsdef

	public void testStandaloneHelloTargetContent() {
		Target target = new HelloTarget("standalone",
				"Hello from standalone target\n");
		evaluator.content(target);
		assertEquals("Hello from standalone target\n", out.toString());
	}

}
