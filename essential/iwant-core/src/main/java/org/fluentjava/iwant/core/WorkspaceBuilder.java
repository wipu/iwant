package org.fluentjava.iwant.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.SortedSet;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment.Variable;

public class WorkspaceBuilder {

	private static final PrintPrefixes PHASE2_PRINT_PREFIXES = PrintPrefixes
			.fromPrefix(":iwant-phase2:");

	public static void main(String[] args) {
		TextOutput.debugLog("WorkspaceBuilder.main: " + Arrays.toString(args));
		try {
			build(args);
		} catch (Exception e) {
			System.err.println(PrintPrefixes.fromSystemProperty().multiLineErr(
					e.getMessage()));
			System.exit(1);
		}
	}

	static void build(String[] args) {
		if (args.length != 4) {
			throw new IllegalArgumentException(
					"Expected arguments wsdefclass wsroot target cache-dir");
		}
		Class<?> wsDef = wsDefClassByName(args[0]);
		String wsRootArg = toAbs(args[1]);
		String targetArg = args[2];
		String cacheDir = toAbs(args[3]);
		String libsDir = toAbs(cacheDir
				+ "/../.internal/iwant/iwant-bootstrapper/phase2/iw/cached/.internal/bin");
		Locations locations = new Locations(wsRootArg, wsRootArg
				+ "/todo-fix-path-to-as-someone", cacheDir, libsDir);
		ContainerPath wsRoot = wsRoot(wsDef, locations);
		NextPhase nextPhase = PathDigger.nextPhase(wsRoot);
		if (nextPhase != null) {
			TextOutput.debugLog("Fresh NextPhase needed: " + nextPhase);
			freshTargetAsPath(nextPhase.classes(), locations);
		}
		if ("list-of/targets".equals(targetArg)) {
			listOfTargets(wsRoot);
			if (nextPhase != null) {
				runNextPhase(nextPhase, targetArg, locations);
			}
		} else {
			String targetName = targetArgumentToTargetName(targetArg);
			Target<?> target = target(wsRoot, targetName);
			if (target != null) {
				String cachedPath = freshTargetAsPath(target, locations);
				System.out.println(PrintPrefixes.fromSystemProperty()
						.outPrefix() + pathToPrint(cachedPath, targetArg));
			} else {
				if (nextPhase != null) {
					runNextPhase(nextPhase, targetArg, locations);
				} else {
					throw new IllegalArgumentException("No such target: "
							+ targetName);
				}
			}
		}
	}

	private static String toAbs(String path) {
		try {
			return new File(path).getCanonicalPath();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static String pathToPrint(String cachedPath, String targetArg) {
		if (targetArg.endsWith("/as-rel-path")) {
			String cwd = System.getProperty("user.dir");
			return cachedPath.replaceFirst("^" + cwd + File.separator, "");
		}
		if (targetArg.endsWith("/as-path")) {
			return cachedPath;
		}
		throw new IllegalArgumentException("Unknown suffix in " + targetArg);
	}

	static String freshTargetAsPath(Target<?> target, Locations locations) {
		TextOutput.debugLog("freshTargetAsPath: " + target);
		try {
			FileUtils.ensureParentDirFor(target.asAbsolutePath(locations));
			FileUtils.ensureParentDirFor(target
					.contentDescriptionCacheDir(locations));
			Refresher.forReal(locations).refresh(target);
			return target.asAbsolutePath(locations);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * TODO robust parsing
	 */
	private static String targetArgumentToTargetName(String targetPath) {
		return targetPath.substring(targetPath.indexOf("/") + 1,
				targetPath.lastIndexOf("/"));
	}

	private static Target<?> target(ContainerPath wsRoot, String targetName) {
		Target<?> target = PathDigger.target(wsRoot, targetName);
		return target;
	}

	private static ContainerPath wsRoot(Class<?> wsDefClass, Locations locations) {
		try {
			WorkspaceDefinition wsDef = (WorkspaceDefinition) wsDefClass
					.newInstance();
			ContainerPath wsRoot = wsDef.wsRoot(locations);
			return wsRoot;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Class<?> wsDefClassByName(String wsDefName) {
		try {
			return Class.forName(wsDefName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static void listOfTargets(ContainerPath wsRoot) {
		SortedSet<Target<?>> targets = wsRoot.targets();
		for (Target<?> target : targets) {
			System.out.println(PrintPrefixes.fromSystemProperty().outPrefix()
					+ target.name());
		}
	}

	static void runNextPhase(NextPhase nextPhase, String targetArg,
			Locations locations) {
		TextOutput.debugLog("runNextPhase starting, targetArg=" + targetArg
				+ ", nextPhase.classes=" + nextPhase.classes());
		JavaClasses classes = nextPhase.classes().content();

		Project project = new Project();
		project.addBuildListener(new Phase2BuildLogger());

		Java java = new Java();
		java.setProject(project);
		java.setFork(true);
		java.setFailonerror(true);

		org.apache.tools.ant.types.Path path = java.createClasspath();
		path.append(antPath(project,
				nextPhase.classes().asAbsolutePath(locations)));
		for (Path cpItem : classes.classpathItems()) {
			path.append(antPath(project, cpItem.asAbsolutePath(locations)));
		}

		// The Java task is broken and doesn't work like Javac, so this hack is
		// needed:
		java.createJvmarg().setValue("-classpath");
		java.createJvmarg().setValue(path.toString());

		Variable sysp = new Variable();
		sysp.setKey(PrintPrefixes.SYSTEM_PROPERTY_NAME);
		sysp.setValue(PHASE2_PRINT_PREFIXES.prefix());
		java.addSysproperty(sysp);

		java.setClassname(WorkspaceBuilder.class.getCanonicalName());
		java.createArg().setValue(nextPhase.className());
		java.createArg().setValue(locations.wsRoot());
		java.createArg().setValue(targetArg);
		java.createArg().setValue(locations.cacheDir());

		java.execute();
	}

	private static class Phase2BuildLogger extends DefaultLogger {

		@Override
		public void messageLogged(BuildEvent e) {
			PrintPrefixes p = PrintPrefixes.fromSystemProperty();
			if (e.getMessage().startsWith(PHASE2_PRINT_PREFIXES.errPrefix())) {
				String rePrefixed = p.errPrefix()
						+ e.getMessage().substring(
								PHASE2_PRINT_PREFIXES.errPrefix().length());
				System.err.println(rePrefixed);
			} else if (e.getMessage().startsWith(
					PHASE2_PRINT_PREFIXES.outPrefix())) {
				String rePrefixed = p.outPrefix()
						+ e.getMessage().substring(
								PHASE2_PRINT_PREFIXES.outPrefix().length());
				System.out.println(rePrefixed);
			} else {
				// just ant noise, we are not printing it
			}
		}

	}

	private static org.apache.tools.ant.types.Path antPath(Project project,
			String name) {
		return new org.apache.tools.ant.types.Path(project, name);
	}

}
