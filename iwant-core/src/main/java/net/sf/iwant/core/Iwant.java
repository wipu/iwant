package net.sf.iwant.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Iwant {

	private static class IwantException extends Exception {

		public IwantException(String message) {
			super(message);
		}

	}

	public static void main(String[] args) throws IOException {
		try {
			targetAsPath(args);
		} catch (IwantException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private static void targetAsPath(String[] args) throws IwantException,
			IOException {
		File iHave = new File(args[0], "i-have");
		String wish = args[1];
		if (!iHave.exists()) {
			throw new IllegalStateException("Internal error: missing " + iHave);
		}
		File wsInfo = new File(iHave, "ws-info.conf");
		if (!wsInfo.exists()) {
			new FileWriter(wsInfo).append(
					"# paths are relative to this file's directory\n"
							+ "WSNAME=example\n" + "WSROOT=../..\n"
							+ "WSDEF_SRC=wsdef\n"
							+ "WSDEF_CLASS=com.example.wsdef.Workspace\n")
					.close();
			throw new IwantException("I created " + wsInfo + " for you."
					+ " Please edit it and rerun me.");
		}
		Properties props = new Properties();
		props.load(new FileReader(wsInfo));
		File wsDefSrc = new File(iHave, props.getProperty("WSDEF_SRC"));
		String wsDefClassName = props.getProperty("WSDEF_CLASS");
		File wsDefJava = wsDefJava(wsDefSrc, wsDefClassName);
		if (!wsDefJava.exists()) {
			ensureDir(wsDefJava.getParentFile());
			new FileWriter(wsDefJava).append(exampleWsDefJava(wsDefClassName))
					.close();
			throw new IwantException("I created " + wsDefJava + " for you."
					+ " Please edit it and rerun me.");
		}
		if ("".equals(wish)) {
			throw new IwantException(usage());
		}
		System.err.println("TODO refresh " + wish + " for " + iHave);
	}

	private static String usage() {
		StringBuilder b = new StringBuilder();
		b.append("Try one of these:\n");
		b.append("  ant list-of-targets\n");
		b.append("  ant -D/target=TARGETNAME\n");
		b.append("    (use tab or ls/dir -D to see valid targets)\n");
		return b.toString();
	}

	private static CharSequence exampleWsDefJava(String wsDefClassName) {
		int lastDotIndex = wsDefClassName.lastIndexOf('.');
		String pack = wsDefClassName.substring(0, lastDotIndex);
		String className = wsDefClassName.substring(lastDotIndex + 1,
				wsDefClassName.length());
		StringBuilder b = new StringBuilder();
		b.append("package " + pack + ";\n");
		b.append("\n");
		b.append("import net.sf.iwant.core.Constant;\n");
		b.append("import net.sf.iwant.core.ContainerPath;\n");
		b.append("import net.sf.iwant.core.EclipseProject;\n");
		b.append("import net.sf.iwant.core.EclipseProjects;\n");
		b.append("import net.sf.iwant.core.Locations;\n");
		b.append("import net.sf.iwant.core.RootPath;\n");
		b.append("import net.sf.iwant.core.Target;\n");
		b.append("import net.sf.iwant.core.WorkspaceDefinition;\n");
		b.append("\n");
		b.append("public class " + className
				+ " implements WorkspaceDefinition {\n");
		b.append("\n");
		b.append("    public ContainerPath wsRoot(Locations locations) {\n");
		b.append("        return new Root(locations);\n");
		b.append("    }\n");
		b.append("\n");
		b.append("    public static class Root extends RootPath {\n");
		b.append("\n");
		b.append("        public Root(Locations locations) {\n");
		b.append("            super(locations);\n");
		b.append("        }\n");
		b.append("\n");
		b.append("        public Target<Constant> aConstant() {\n");
		b.append("            return target(\"aConstant\").\n");
		b.append("                content(Constant.value(\"Constant generated content\\n\")).end();\n");
		b.append("        }\n");
		b.append("\n");
		b.append("        public Target<EclipseProjects> eclipseProjects() {\n");
		b.append("            return target(\"eclipse-projects\").\n");
		b.append("                content(EclipseProjects.with().\n");
		b.append("                    project(wsdefEclipseProject())).end();\n");
		b.append("        }\n");
		b.append("\n");
		b.append("        public EclipseProject wsdefEclipseProject() {\n");
		b.append("            return EclipseProject.with().name(\"as-$WSNAME-developer\").\n");
		b.append("                src(\"i-have/wsdef\").libs(builtin().all()).end();\n");
		b.append("        }\n");
		b.append("\n");
		b.append("    }\n");
		b.append("\n");
		b.append("}\n");
		return b;
	}

	private static File wsDefJava(File wsDefSrc, String wsDefClassName) {
		return new File(wsDefSrc, wsDefClassName.replaceAll("\\.", "/")
				+ ".java");
	}

	/**
	 * TODO replace all copies of this with one
	 */
	private static void ensureDir(File dir) {
		File parent = dir.getParentFile();
		if (!parent.exists()) {
			ensureDir(parent);
		}
		dir.mkdir();
	}

}
