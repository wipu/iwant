package net.sf.iwant.api;

import junit.framework.TestCase;
import net.sf.iwant.api.CodeFormatterPolicy.FormatterSettingsListener;
import net.sf.iwant.api.CodeFormatterPolicy.TabulationCharValue;

public class CodeFormatterPolicyTest extends TestCase implements
		FormatterSettingsListener {

	private Integer alignmentForEnumConstants;
	private TabulationCharValue tabulationChar;

	@Override
	protected void setUp() throws Exception {
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

	public void testWriteCallsListenerWithDefaultValues() {
		CodeFormatterPolicy policy = new CodeFormatterPolicy();

		policy.write(this);

		assertEquals(Integer.valueOf(0), alignmentForEnumConstants);
		assertEquals(TabulationCharValue.TAB, tabulationChar);
	}

	public void testWriteCallsListenerWithChangedValues() {
		CodeFormatterPolicy policy = new CodeFormatterPolicy();
		policy.alignmentForEnumConstants = 48;
		policy.tabulationChar = TabulationCharValue.SPACE;

		policy.write(this);

		assertEquals(Integer.valueOf(48), alignmentForEnumConstants);
		assertEquals(TabulationCharValue.SPACE, tabulationChar);
	}

}
