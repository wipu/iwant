package net.sf.iwant.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import net.sf.iwant.api.javamodules.CodeFormatterPolicy;
import net.sf.iwant.api.javamodules.CodeStyle;
import net.sf.iwant.api.javamodules.CodeStylePolicy;
import net.sf.iwant.api.javamodules.CodeStyleValue;
import net.sf.iwant.api.javamodules.JavaCompliance;
import net.sf.iwant.eclipsesettings.OrgEclipseJdtCorePrefs;

public class ImpossibleEnumSwitchCasesTest {

	@Test
	public void illegalCodeStyleAsPropertyLine() {
		OrgEclipseJdtCorePrefs prefs = new OrgEclipseJdtCorePrefs(
				CodeStylePolicy.defaultsExcept().end(),
				CodeFormatterPolicy.defaults(), JavaCompliance.JAVA_1_8);

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