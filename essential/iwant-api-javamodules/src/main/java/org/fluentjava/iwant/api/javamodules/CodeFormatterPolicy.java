package org.fluentjava.iwant.api.javamodules;

public class CodeFormatterPolicy {

	public Integer alignmentForEnumConstants = 0;
	public TabulationCharValue tabulationChar = TabulationCharValue.TAB;
	public Integer lineSplit = 80;

	public static CodeFormatterPolicy defaults() {
		return new CodeFormatterPolicy();
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alignmentForEnumConstants == null) ? 0
				: alignmentForEnumConstants.hashCode());
		result = prime * result
				+ ((lineSplit == null) ? 0 : lineSplit.hashCode());
		result = prime * result
				+ ((tabulationChar == null) ? 0 : tabulationChar.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CodeFormatterPolicy other = (CodeFormatterPolicy) obj;
		if (alignmentForEnumConstants == null) {
			if (other.alignmentForEnumConstants != null) {
				return false;
			}
		} else if (!alignmentForEnumConstants
				.equals(other.alignmentForEnumConstants)) {
			return false;
		}
		if (lineSplit == null) {
			if (other.lineSplit != null) {
				return false;
			}
		} else if (!lineSplit.equals(other.lineSplit)) {
			return false;
		}
		if (tabulationChar != other.tabulationChar) {
			return false;
		}
		return true;
	}

}
