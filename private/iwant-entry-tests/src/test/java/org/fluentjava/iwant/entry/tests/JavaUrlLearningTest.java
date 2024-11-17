package org.fluentjava.iwant.entry.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

public class JavaUrlLearningTest {

	@Test
	public void urlAppendForLearning() throws MalformedURLException {
		URL base = new URL("http://localhost/base");
		// it replaces the path:
		assertEquals("http://localhost/sub",
				new URL(base, "sub").toExternalForm());
		// so we need string append here:
		assertEquals("http://localhost/base/sub",
				new URL(base + "/sub").toExternalForm());
	}

	@Test
	public void invalidUrlForTestingErrors() {
		try {
			@SuppressWarnings("unused")
			URL url = new URL("crap");
			fail();
		} catch (MalformedURLException e) {
			assertEquals("no protocol: crap", e.getMessage());
		}
	}

}
