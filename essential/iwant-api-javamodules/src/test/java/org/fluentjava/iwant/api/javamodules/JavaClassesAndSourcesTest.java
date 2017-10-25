package org.fluentjava.iwant.api.javamodules;

import junit.framework.TestCase;
import org.fluentjava.iwant.api.model.Source;

public class JavaClassesAndSourcesTest extends TestCase {

	public void testNameIsThatOfClasses() {
		assertEquals("one", new JavaClassesAndSources(Source.underWsroot("one"),
				Source.underWsroot("irrelevant")).name());
		assertEquals("two", new JavaClassesAndSources(Source.underWsroot("two"),
				Source.underWsroot("irrelevant")).name());
	}

}
