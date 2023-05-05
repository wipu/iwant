package org.fluentjava.iwant.api.javamodules;

public class JavaCompliance {

	public static final JavaCompliance JAVA_1_6 = of("1.6");
	public static final JavaCompliance JAVA_1_7 = of("1.7");
	public static final JavaCompliance JAVA_1_8 = of("1.8");
	public static final JavaCompliance JAVA_11 = of("11");
	public static final JavaCompliance JAVA_17 = of("17");

	private final String prettyName;

	private JavaCompliance(String prettyName) {
		this.prettyName = prettyName;
	}

	public static JavaCompliance of(String prettyName) {
		return new JavaCompliance(prettyName);
	}

	public String prettyName() {
		return prettyName;
	}

	@Override
	public String toString() {
		return prettyName;
	}

}
