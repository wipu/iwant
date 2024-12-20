package org.fluentjava.iwant.api.javamodules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class JavaComplianceTest {

	@Test
	public void toStringIsReadable() {
		assertEquals("1.6", JavaCompliance.JAVA_1_6.toString());
		assertEquals("11", JavaCompliance.JAVA_11.toString());
		assertEquals("17", JavaCompliance.JAVA_17.toString());
		assertEquals("17", JavaCompliance.of("17").toString());
	}

}
