package net.sf.iwant.eclipsesettings;

public class DotProject {

	private final String name;

	public DotProject(String name) {
		this.name = name;
	}

	public static DotProjectSpex named(String name) {
		return new DotProjectSpex(name);
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
		b.append("                <buildCommand>\n");
		b.append("                        <name>org.eclipse.jdt.core.javabuilder</name>\n");
		b.append("                        <arguments>\n");
		b.append("                        </arguments>\n");
		b.append("                </buildCommand>\n");
		b.append("        </buildSpec>\n");
		b.append("        <natures>\n");
		b.append("                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		b.append("        </natures>\n");
		b.append("</projectDescription>\n");
		return b.toString();
	}

	public static class DotProjectSpex {

		private final String name;

		public DotProjectSpex(String name) {
			this.name = name;
		}

		public DotProject end() {
			return new DotProject(name);
		}

	}

}
