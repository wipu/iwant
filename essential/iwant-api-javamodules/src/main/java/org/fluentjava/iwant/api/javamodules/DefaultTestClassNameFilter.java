package net.sf.iwant.api.javamodules;

import net.sf.iwant.api.model.StringFilter;

public class DefaultTestClassNameFilter implements StringFilter {

	@Override
	public boolean matches(String candidate) {
		boolean namedLikeTest = candidate.matches(".*Test$")
				&& !candidate.matches(".*Abstract[^.]*Test$");
		if (namedLikeTest && candidate.contains("$")) {
			return false;
		}
		return namedLikeTest;
	}

	@Override
	public String toString() {
		return getClass().getCanonicalName();
	}

}
