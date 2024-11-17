package org.fluentjava.iwant.api.javamodules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fluentjava.iwant.api.model.Source;
import org.junit.jupiter.api.Test;

public class JavaClassesAndSourcesTest {

	@Test
	public void nameIsThatOfClasses() {
		assertEquals("one", new JavaClassesAndSources(Source.underWsroot("one"),
				Source.underWsroot("irrelevant")).name());
		assertEquals("two", new JavaClassesAndSources(Source.underWsroot("two"),
				Source.underWsroot("irrelevant")).name());
	}

}
