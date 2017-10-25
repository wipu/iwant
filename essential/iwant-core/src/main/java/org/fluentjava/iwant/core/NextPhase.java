package org.fluentjava.iwant.core;

public class NextPhase {

	private final Target<JavaClasses> classes;
	private final String className;

	public static NextPhaseBuilder at(Target<JavaClasses> classes) {
		return new NextPhaseBuilder(classes);
	}

	public static class NextPhaseBuilder {

		private final Target<JavaClasses> classes;

		private NextPhaseBuilder(Target<JavaClasses> classes) {
			this.classes = classes;
		}

		public NextPhase named(String className) {
			return new NextPhase(classes, className);
		}

	}

	private NextPhase(Target<JavaClasses> classes, String className) {
		this.classes = classes;
		this.className = className;
	}

	public Target<JavaClasses> classes() {
		return classes;
	}

	public String className() {
		return className;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("NextPhase {\n");
		b.append("  at:").append(classes).append("\n");
		b.append("  className:").append(className).append("\n");
		b.append("}\n");
		return b.toString();
	}

}
