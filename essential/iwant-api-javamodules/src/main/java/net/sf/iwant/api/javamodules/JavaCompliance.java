package net.sf.iwant.api.javamodules;

public enum JavaCompliance {

	JAVA_1_6("1.6"), JAVA_1_7("1.7");

	private final String prettyName;

	JavaCompliance(String prettyName) {
		this.prettyName = prettyName;
	}

	public String prettyName() {
		return prettyName;
	}

}
