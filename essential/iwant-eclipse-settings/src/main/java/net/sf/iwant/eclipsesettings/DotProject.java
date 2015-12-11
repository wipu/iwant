package net.sf.iwant.eclipsesettings;

public class DotProject {

	private final String name;
	private final boolean hasExternalBuilder;
	private final boolean hasScalaSupport;

	public DotProject(String name, boolean hasExternalBuilder,
			boolean hasScalaSupport) {
		this.name = name;
		this.hasExternalBuilder = hasExternalBuilder;
		this.hasScalaSupport = hasScalaSupport;
	}

	public static DotProjectSpex named(String name) {
		return new DotProjectSpex(name);
	}

	private String builderName() {
		return hasScalaSupport ? "org.scala-ide.sdt.core.scalabuilder"
				: "org.eclipse.jdt.core.javabuilder";
	}

	public String name() {
		return name;
	}

	public boolean hasScalaSupport() {
		return hasScalaSupport;
	}

	public String asFileContent() {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		b.append("<projectDescription>\n");
		b.append("        <name>" + name + "</name>\n");
		b.append("        <comment></comment>\n");
		b.append("        <projects>\n");
		b.append("        </projects>\n");
		b.append("        <buildSpec>\n");
		if (hasExternalBuilder) {
			b.append("                <buildCommand>\n");
			b.append(
					"                        <name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n");
			b.append("                        <arguments>\n");
			b.append("                                <dictionary>\n");
			b.append(
					"                                        <key>LaunchConfigHandle</key>\n");
			b.append(
					"                                        <value>&lt;project&gt;/.externalToolBuilders/"
							+ name() + ".launch</value>\n");
			b.append("                                </dictionary>\n");
			b.append("                        </arguments>\n");
			b.append("                </buildCommand>\n");
		}
		b.append("                <buildCommand>\n");
		b.append(
				"                        <name>" + builderName() + "</name>\n");
		b.append("                        <arguments>\n");
		b.append("                        </arguments>\n");
		b.append("                </buildCommand>\n");
		b.append("        </buildSpec>\n");
		b.append("        <natures>\n");
		if (hasScalaSupport) {
			b.append(
					"                <nature>org.scala-ide.sdt.core.scalanature</nature>\n");
		}
		b.append(
				"                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		b.append("        </natures>\n");
		b.append("</projectDescription>\n");
		return b.toString();
	}

	public boolean hasExternalBuilder() {
		return hasExternalBuilder;
	}

	public static class DotProjectSpex {

		private final String name;
		private boolean hasExternalBuilder;
		private boolean hasScalaSupport;

		public DotProjectSpex(String name) {
			this.name = name;
		}

		public DotProjectSpex hasExternalBuilder(boolean hasExternalBuilder) {
			this.hasExternalBuilder = hasExternalBuilder;
			return this;
		}

		public DotProjectSpex hasScalaSupport(boolean hasScalaSupport) {
			this.hasScalaSupport = hasScalaSupport;
			return this;
		}

		public DotProject end() {
			return new DotProject(name, hasExternalBuilder, hasScalaSupport);
		}

	}

}
