package net.sf.iwant.core;

import junit.framework.TestCase;

public class ContentDefinitionsTest extends TestCase {

	private static final Locations LOCATIONS = new Locations("ws", "cache");

	private String descr;

	public void setUp() {
		descr = "";
	}

	private void descrChanged(Content content) {
		String newValue = content.definitionDescription();
		if (newValue.equals(descr)) {
			fail("Didn't change:" + descr);
		}
		descr = newValue;
	}

	// the tests

	public void testConstant() {
		Constant content = Constant.value("value");
		descrChanged(content);
		content = Constant.value("some-other-value");
		descrChanged(content);
	}

	public void testJavaClasses() {
		JavaClasses content = JavaClasses.compiledFrom(new Source("src",
				LOCATIONS));
		descrChanged(content);
		content = JavaClasses.compiledFrom(new Source("other-src", LOCATIONS));
		descrChanged(content);
		content = content.using(new Path("some-classes"));
		descrChanged(content);
		content.using(new Path("some-other-classes"));
		descrChanged(content);
	}

	public void testJunitResult() {
		JunitResult content = JunitResult.ofClass("ATest");
		descrChanged(content);
		content = JunitResult.ofClass("SomeOtherTest");
		descrChanged(content);
		content = content.using(new Path("some-classes"));
		descrChanged(content);
		content.using(new Path("some-other-classes"));
		descrChanged(content);
	}

}