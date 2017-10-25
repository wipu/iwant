package org.fluentjava.iwant.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Properties;

import org.apache.tools.ant.ExitStatusException;

@SuppressWarnings("resource")
public class Iwant {

	static class IwantException extends RuntimeException {

		public IwantException(String message) {
			super(message);
		}

		public IwantException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	public static void main(String[] args) throws Exception {
		TextOutput.debugLog("\n --- Iwant.main: " + Arrays.toString(args)
				+ "\n");
		try {
			wish(args);
		} catch (IwantException e) {
			System.err.println(PrintPrefixes.fromSystemProperty().multiLineErr(
					e.getMessage()));
			System.exit(1);
		} catch (Exception e) {
			System.err.println(PrintPrefixes.fromSystemProperty().multiLineErr(
					e.getMessage()));
			// TODO test and implement prefixed stack trace and exit 1
			throw e;
		}
	}

	private static void wish(String[] args) throws IwantException, IOException {
		File iHave = new File(args[0], "i-have");
		String wish = args[1];
		File iwantLibs = new File(args[2]);
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
		String wsName = props.getProperty("WSNAME");
		File wsRoot = new File(iHave, props.getProperty("WSROOT"));
		File wsDefSrc = new File(iHave, props.getProperty("WSDEF_SRC"));
		String wsDefClassName = props.getProperty("WSDEF_CLASS");
		File wsDefJava = wsDefJava(wsDefSrc, wsDefClassName);
		if (!wsDefJava.exists()) {
			FileUtils.ensureDir(wsDefJava.getParentFile());
			new FileWriter(wsDefJava).append(
					exampleWsDefJava(wsDefClassName, wsName)).close();
			throw new IwantException("I created " + wsDefJava + " for you."
					+ " Please edit it and rerun me.");
		}
		Locations locations = Locations.from(wsRoot, iHave, wsName, iwantLibs);
		Target<JavaClasses> wsDefClasses = wsDefClassesAsFreshTarget(locations,
				wsDefSrc, wsName);
		NextPhase nextPhase = NextPhase.at(wsDefClasses).named(wsDefClassName);
		regenerateWishScripts(locations, nextPhase);
		if ("".equals(wish)) {
			throw new IwantException(usage());
		}
		refreshAndPrintUsingWsDefClasses(locations, wish, nextPhase);
	}

	private static void regenerateWishScripts(Locations locations,
			NextPhase nextPhase) throws IwantException, IOException {
		TextOutput.debugLog("Regenerating wish scripts.");
		String listOfTargetsString = listOfTargetsString(locations, nextPhase);

		File listOfTargetsWish = new File(locations.iwant()
				+ "/list-of/targets");
		generateWishScript("list-of/targets", listOfTargetsWish, "..");

		FileUtils.ensureEmpty(locations.iwant() + "/target");

		BufferedReader reader = new BufferedReader(new StringReader(
				listOfTargetsString));
		String targetName;
		while ((targetName = reader.readLine()) != null) {
			targetName = targetName.replaceFirst("^"
					+ PrintPrefixes.fromSystemProperty().outPrefix(), "");
			File targetWish = new File(locations.iwant() + "/target/"
					+ targetName + "/as-path");
			// TODO allow / in target name => more .. in rel path:
			generateWishScript(targetName, targetWish, "../..");
		}
	}

	private static void generateWishScript(String name, File to,
			String relPathToIwant) throws IOException {
		TextOutput.debugLog("Generating wish script: " + name);
		FileUtils.ensureParentDirFor(to.getCanonicalPath());
		FileWriter f = new FileWriter(to);
		f.append("#!/bin/bash\n");
		f.append("HERE=$(dirname \"$0\")\n");
		f.append("exec \"$HERE/").append(relPathToIwant)
				.append("/help.sh\" -D/target=").append(name).append("\n");
		f.close();
		to.setExecutable(true);
	}

	private static String listOfTargetsString(Locations locations,
			NextPhase nextPhase) throws IwantException {
		PrintStream origOut = System.out;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		try {
			refreshAndPrintUsingWsDefClasses(locations, "list-of/targets",
					nextPhase);
		} finally {
			System.setOut(origOut);
		}
		return out.toString();
	}

	/**
	 * TODO find a better way, this is ugly
	 * 
	 * Or if this proves to be a handy class, make it public.
	 */
	private static class AbsoluteSource extends Source {

		public AbsoluteSource(File file) throws IOException {
			super(file.getCanonicalPath());
		}

		@Override
		public String asAbsolutePath(Locations locations) {
			return name();
		}

	}

	private static void refreshAndPrintUsingWsDefClasses(Locations locations,
			String wish, NextPhase nextPhase) throws IwantException {
		try {
			// TODO just pass wish as such:
			String effectiveWish = "list-of/targets".equals(wish) ? wish
					: "target/" + wish + "/as-path";
			WorkspaceBuilder.runNextPhase(nextPhase, effectiveWish, locations);
		} catch (ExitStatusException e) {
			throw new IwantException("Refresh failed.", e);
		}
	}

	private static Target<JavaClasses> wsDefClassesAsFreshTarget(
			Locations locations, File wsDefSrcFile, String wsName)
			throws IOException {
		Source wsDefSrc = new AbsoluteSource(wsDefSrcFile);
		Builtins builtins = new Builtins(locations);
		WsDefClassesTarget wsDefClasses = new WsDefClassesTarget(wsName,
				JavaClasses.compiledFrom(wsDefSrc).using(builtins.all()));
		WorkspaceBuilder.freshTargetAsPath(wsDefClasses, locations);
		return wsDefClasses;
	}

	private static String usage() {
		StringBuilder b = new StringBuilder();
		b.append("Please tell what you want.\n\n");
		b.append("Ant usage:\n");
		b.append("  as-someone/with/ant/iw $ ant list-of-targets\n");
		b.append("  as-someone/with/ant/iw $ ant -D/target=TARGETNAME\n");
		b.append("Shell usage:\n");
		b.append("  as-someone/with/bash $ iwant/list-of/targets\n");
		b.append("  as-someone/with/bash $ iwant/target/TARGETNAME/as-path");
		return b.toString();
	}

	private static CharSequence exampleWsDefJava(String wsDefClassName,
			String wsName) {
		int lastDotIndex = wsDefClassName.lastIndexOf('.');
		String pack = wsDefClassName.substring(0, lastDotIndex);
		String className = wsDefClassName.substring(lastDotIndex + 1,
				wsDefClassName.length());
		StringBuilder b = new StringBuilder();
		b.append("package " + pack + ";\n");
		b.append("\n");
		b.append("import org.fluentjava.iwant.core.Constant;\n");
		b.append("import org.fluentjava.iwant.core.ContainerPath;\n");
		b.append("import org.fluentjava.iwant.core.EclipseProject;\n");
		b.append("import org.fluentjava.iwant.core.EclipseProjects;\n");
		b.append("import org.fluentjava.iwant.core.Locations;\n");
		b.append("import org.fluentjava.iwant.core.RootPath;\n");
		b.append("import org.fluentjava.iwant.core.Target;\n");
		b.append("import org.fluentjava.iwant.core.WorkspaceDefinition;\n");
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
		b.append("            return EclipseProject.with().name(\"as-" + wsName
				+ "-developer\").\n");
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

}
