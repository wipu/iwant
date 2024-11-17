package org.fluentjava.iwant.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.fluentjava.iwant.api.javamodules.CodeFormatterPolicy;
import org.fluentjava.iwant.api.javamodules.CodeStyle;
import org.fluentjava.iwant.api.javamodules.CodeStylePolicy;
import org.fluentjava.iwant.api.javamodules.CodeStyleValue;
import org.fluentjava.iwant.api.javamodules.JavaCompliance;
import org.fluentjava.iwant.eclipsesettings.OrgEclipseJdtCorePrefs;
import org.junit.jupiter.api.Test;

public class ImpossibleEnumSwitchCasesTest {

	@Test
	public void illegalCodeStyleAsPropertyLine() {
		OrgEclipseJdtCorePrefs prefs = new OrgEclipseJdtCorePrefs(
				CodeStylePolicy.defaultsExcept().end(),
				CodeFormatterPolicy.defaults(), JavaCompliance.JAVA_1_8, false);

		try {
			prefs.asPropertyLine(CodeStyle._ILLEGAL_);
			fail();
		} catch (UnsupportedOperationException e) {
			assertEquals("Unsupported style: _ILLEGAL_", e.getMessage());
		}
	}

	@Test
	public void illegalCodeStyleValueAsPropertyLine() {
		try {
			OrgEclipseJdtCorePrefs.valueToEclipseValue(true,
					CodeStyleValue._ILLEGAL_);
			fail();
		} catch (UnsupportedOperationException e) {
			assertEquals("Unsupported value _ILLEGAL_", e.getMessage());
		}
	}

}
