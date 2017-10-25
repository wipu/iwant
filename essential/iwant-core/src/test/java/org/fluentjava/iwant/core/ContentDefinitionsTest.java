package org.fluentjava.iwant.core;

import junit.framework.TestCase;

public class ContentDefinitionsTest extends TestCase {

	private String descr;

	@Override
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
		JavaClasses content = JavaClasses.compiledFrom(new Source("src"));
		descrChanged(content);
		content = JavaClasses.compiledFrom(new Source("other-src"));
		descrChanged(content);
		content = content.using(new Source("some-classes"));
		descrChanged(content);
		content.using(new Source("some-other-classes"));
		descrChanged(content);
	}

	public void testJunitResult() {
		JunitResult content = JunitResult.ofClass("ATest");
		descrChanged(content);
		content = JunitResult.ofClass("SomeOtherTest");
		descrChanged(content);
		content = content.using(new Source("some-classes"));
		descrChanged(content);
		content.using(new Source("some-other-classes"));
		descrChanged(content);
	}

}
