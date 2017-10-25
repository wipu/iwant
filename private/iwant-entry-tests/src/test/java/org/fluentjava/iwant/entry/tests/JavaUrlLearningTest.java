package org.fluentjava.iwant.entry.tests;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

public class JavaUrlLearningTest extends TestCase {

	public void testUrlAppendForLearning() throws MalformedURLException {
		URL base = new URL("http://localhost/base");
		// it replaces the path:
		assertEquals("http://localhost/sub",
				new URL(base, "sub").toExternalForm());
		// so we need string append here:
		assertEquals("http://localhost/base/sub",
				new URL(base + "/sub").toExternalForm());
	}

	public void testInvalidUrlForTestingErrors() {
		try {
			@SuppressWarnings("unused")
			URL url = new URL("crap");
			fail();
		} catch (MalformedURLException e) {
			assertEquals("no protocol: crap", e.getMessage());
		}
	}

}
