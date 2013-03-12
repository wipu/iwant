package net.sf.iwant.api;

import junit.framework.TestCase;

public class CodeStylePolicyTest extends TestCase {

	public void testDefaults() {
		CodeStylePolicy policy = CodeStylePolicy.defaultsExcept().end();

		assertEquals(CodeStyleValue.FAIL,
				policy.valueOf(CodeStyle.ASSERT_IDENTIFIER));
		assertEquals(CodeStyleValue.WARN, policy.valueOf(CodeStyle.DEAD_CODE));
		assertEquals(CodeStyleValue.IGNORE,
				policy.valueOf(CodeStyle.DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals(CodeStyleValue.IGNORE,
				policy.valueOf(CodeStyle.NON_EXTERNALIZED_STRING_LITERAL));
	}

	public void testOverrides() {
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

	public void testIteratingStylesFromDefaultGivesNonNullValues() {
		CodeStylePolicy policy = CodeStylePolicy.defaultsExcept().end();

		for (CodeStyle style : CodeStyle.values()) {
			CodeStyleValue value = policy.valueOf(style);
			assertNotNull("Please define default for " + style, value);
		}
	}

}
