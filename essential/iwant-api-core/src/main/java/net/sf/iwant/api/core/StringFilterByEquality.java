package net.sf.iwant.api.core;

import net.sf.iwant.api.model.StringFilter;

public class StringFilterByEquality implements StringFilter {

	private String value;

	public StringFilterByEquality(String value) {
		this.value = value;
	}

	@Override
	public boolean matches(String candidate) {
		return value.equals(candidate);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + value;
	}

	public String value() {
		return value;
	}

}
