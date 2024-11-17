package org.fluentjava.iwant.api.javamodules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fluentjava.iwant.api.javamodules.CodeFormatterPolicy.FormatterSettingsListener;
import org.fluentjava.iwant.api.javamodules.CodeFormatterPolicy.TabulationCharValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.testing.EqualsTester;

public class CodeFormatterPolicyTest implements FormatterSettingsListener {

	private Integer alignmentForEnumConstants;
	private TabulationCharValue tabulationChar;

	@BeforeEach
	protected void before() throws Exception {
		alignmentForEnumConstants = null;
		tabulationChar = null;
	}

	@Override
	public void alignmentForEnumConstants(Integer value) {
		this.alignmentForEnumConstants = value;
	}

	@Override
	public void tabulationChar(TabulationCharValue value) {
		this.tabulationChar = value;
	}

	// the tests

	@Test
	public void writeCallsListenerWithDefaultValues() {
		CodeFormatterPolicy policy = new CodeFormatterPolicy();

		policy.write(this);

		assertEquals(Integer.valueOf(0), alignmentForEnumConstants);
		assertEquals(TabulationCharValue.TAB, tabulationChar);
	}

	@Test
	public void writeCallsListenerWithChangedValues() {
		CodeFormatterPolicy policy = new CodeFormatterPolicy();
		policy.alignmentForEnumConstants = 48;
		policy.tabulationChar = TabulationCharValue.SPACE;

		policy.write(this);

		assertEquals(Integer.valueOf(48), alignmentForEnumConstants);
		assertEquals(TabulationCharValue.SPACE, tabulationChar);
	}

	@Test
	public void equalsAndHashcode() {
		EqualsTester et = new EqualsTester();
		et.addEqualityGroup(CodeFormatterPolicy.defaults(),
				CodeFormatterPolicy.defaults(), new CodeFormatterPolicy());

		CodeFormatterPolicy alignmentForEnumConstants = new CodeFormatterPolicy();
		alignmentForEnumConstants.alignmentForEnumConstants = 100;
		et.addEqualityGroup(alignmentForEnumConstants);

		CodeFormatterPolicy lineSplit = new CodeFormatterPolicy();
		lineSplit.lineSplit = 120;
		et.addEqualityGroup(lineSplit);

		CodeFormatterPolicy tabulationChar = new CodeFormatterPolicy();
		tabulationChar.tabulationChar = TabulationCharValue.SPACE;
		et.addEqualityGroup(tabulationChar);

		et.testEquals();
	}

}
