package net.sf.iwant.api.javamodules;

public class CodeFormatterPolicy {

	public Integer alignmentForEnumConstants = 0;
	public TabulationCharValue tabulationChar = TabulationCharValue.TAB;
	public Integer lineSplit = 80;

	public enum TabulationCharValue {

		TAB, SPACE,

	}

	public interface FormatterSettingsListener {

		void alignmentForEnumConstants(Integer value);

		void tabulationChar(TabulationCharValue value);

	}

	public void write(FormatterSettingsListener out) {
		out.alignmentForEnumConstants(alignmentForEnumConstants);
		out.tabulationChar(tabulationChar);
	}

}
