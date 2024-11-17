package org.fluentjava.iwant.api.javamodules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class CodeStylePolicyTest {

	@Test
	public void defaults() {
		CodeStylePolicy policy = CodeStylePolicy.defaultsExcept().end();

		assertEquals(CodeStyleValue.FAIL,
				policy.valueOf(CodeStyle.ASSERT_IDENTIFIER));
		assertEquals(CodeStyleValue.WARN, policy.valueOf(CodeStyle.DEAD_CODE));
		assertEquals(CodeStyleValue.IGNORE,
				policy.valueOf(CodeStyle.DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals(CodeStyleValue.IGNORE,
				policy.valueOf(CodeStyle.NON_EXTERNALIZED_STRING_LITERAL));
	}

	@Test
	public void overrides() {
		CodeStylePolicy policy = CodeStylePolicy.defaultsExcept()
				.ignore(CodeStyle.ASSERT_IDENTIFIER)
				.warn(CodeStyle.NON_EXTERNALIZED_STRING_LITERAL)
				.fail(CodeStyle.DEAD_CODE).end();

		// overrides:
		assertEquals(CodeStyleValue.IGNORE,
				policy.valueOf(CodeStyle.ASSERT_IDENTIFIER));
		assertEquals(CodeStyleValue.FAIL, policy.valueOf(CodeStyle.DEAD_CODE));
		assertEquals(CodeStyleValue.WARN,
				policy.valueOf(CodeStyle.NON_EXTERNALIZED_STRING_LITERAL));
		// not affected default:
		assertEquals(CodeStyleValue.IGNORE,
				policy.valueOf(CodeStyle.DEPRECATION_IN_DEPRECATED_CODE));
	}

	@Test
	public void iteratingStylesFromDefaultGivesNonNullValues() {
		CodeStylePolicy policy = CodeStylePolicy.defaultsExcept().end();

		for (CodeStyle style : CodeStyle.values()) {
			CodeStyleValue value = policy.valueOf(style);
			assertNotNull(value, "Please define default for " + style);
		}
	}

}
