package net.sf.iwant.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.SortedSet;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;

public class WorkspaceBuilder {

	public static void main(String[] args) {
		if (args.length != 4) {
			throw new IllegalArgumentException(
					"Expected arguments wsdefclass wsroot target cache-dir");
		}
		Class<?> wsDef = wsDefClassByName(args[0]);
		String wsRootArg = toAbs(args[1]);
		String targetArg = args[2];
		String cacheDir = toAbs(args[3]);
		Locations locations = new Locations(wsRootArg, cacheDir, cacheDir
				+ "/todo-fix-path-to-iwant-libs");
		ContainerPath wsRoot = wsRoot(wsDef, locations);
		NextPhase nextPhase = PathDigger.nextPhase(wsRoot);
		if (nextPhase != null) {
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
		try {
			// TODO only 1 places for building these paths:
			ensureParentDirFor(locations.targetCacheDir() + "/" + target.name());
			ensureParentDirFor(locations.contentDescriptionCacheDir() + "/"
					+ target.name());
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

	private static void ensureParentDirFor(String fileName) {
		File file = new File(fileName);
		File parent = file.getParentFile();
		ensureDir(parent);
	}

	private static void ensureDir(File dir) {
		File parent = dir.getParentFile();
		if (!parent.exists())
			ensureDir(parent);
		if (!dir.exists())
			dir.mkdir();
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
		SortedSet<Target<?>> targets = PathDigger.targets(wsRoot);
		for (Target<?> target : targets) {
			System.out.println(PrintPrefixes.fromSystemProperty().outPrefix()
					+ target.name());
		}
	}

	static void runNextPhase(NextPhase nextPhase, String targetArg,
			Locations locations) {
		JavaClasses classes = nextPhase.classes().content();
		Project project = new Project();
		Java java = new Java();
		java.setProject(project);

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

		java.createJvmarg().setValue(
				"-Diwant-print-prefix="
						+ PrintPrefixes.fromSystemProperty().prefix());

		java.setClassname(WorkspaceBuilder.class.getCanonicalName());
		java.createArg().setValue(nextPhase.className());
		java.createArg().setValue(locations.wsRoot());
		java.createArg().setValue(targetArg);
		java.createArg().setValue(locations.cacheDir());
		java.setFork(true);
		java.setFailonerror(true);
		// TODO test with a longer chain and generate unique names:
		File err = new File(locations.cacheDir() + "/nextPhase-err");
		File out = new File(locations.cacheDir() + "/nextPhase-out");
		java.setError(err);
		java.setOutput(out);

		try {
			java.execute();
		} finally {
			print(out, System.out);
			print(err, System.err);
		}
	}

	private static void print(File file, PrintStream out) {
		if (!file.exists()) {
			return;
		}
		try {
			FileReader reader = new FileReader(file);
			while (true) {
				int c = reader.read();
				if (c < 0) {
					reader.close();
					return;
				}
				out.print((char) c);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static org.apache.tools.ant.types.Path antPath(Project project,
			String name) {
		return new org.apache.tools.ant.types.Path(project, name);
	}

}
