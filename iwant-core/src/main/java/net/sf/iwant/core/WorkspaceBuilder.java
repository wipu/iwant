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
		Class wsDef = wsDefClassByName(args[0]);
		String wsRootArg = toAbs(args[1]);
		String targetArg = args[2];
		String cacheDir = toAbs(args[3]);
		Locations locations = new Locations(wsRootArg, cacheDir);
		ContainerPath wsRoot = wsRoot(wsDef, locations);
		NextPhase nextPhase = PathDigger.nextPhase(wsRoot);
		if (nextPhase != null) {
			freshTargetAsPath(nextPhase.classes(), locations);
		}
		if ("list-of/targets".equals(targetArg)) {
			listOfTargets(wsRoot);
			if (nextPhase != null) {
				runNextPhase(nextPhase, wsRootArg, targetArg, cacheDir);
			}
		} else {
			String targetName = targetArgumentToTargetName(targetArg);
			Target target = target(wsRoot, targetName);
			if (target != null) {
				String cachedPath = freshTargetAsPath(target, locations);
				System.out.println(pathToPrint(cachedPath, targetArg));
			} else {
				if (nextPhase != null) {
					runNextPhase(nextPhase, wsRootArg, targetArg, cacheDir);
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

	private static String freshTargetAsPath(Target target, Locations locations) {
		try {
			ensureParentDirFor(locations.targetCacheDir() + "/"
					+ target.nameWithoutCacheDir());
			ensureParentDirFor(locations.contentDescriptionCacheDir() + "/"
					+ target.nameWithoutCacheDir());
			Refresher.forReal(locations).refresh(target);
			return target.name();
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

	private static Target target(ContainerPath wsRoot, String targetName) {
		Target target = PathDigger.target(wsRoot, targetName);
		return target;
	}

	private static ContainerPath wsRoot(Class wsDefClass, Locations locations) {
		try {
			WorkspaceDefinition wsDef = (WorkspaceDefinition) wsDefClass
					.newInstance();
			ContainerPath wsRoot = wsDef.wsRoot(locations);
			return wsRoot;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Class wsDefClassByName(String wsDefName) {
		try {
			return Class.forName(wsDefName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static void listOfTargets(ContainerPath wsRoot) {
		SortedSet<Target> targets = PathDigger.targets(wsRoot);
		for (Target target : targets) {
			System.out.println(target.nameWithoutCacheDir());
		}
	}

	private static void runNextPhase(NextPhase nextPhase, String wsRoot,
			String targetArg, String cacheDir) {
		JavaClasses classes = (JavaClasses) nextPhase.classes().content();
		Project project = new Project();
		Java java = new Java();
		java.setProject(project);

		org.apache.tools.ant.types.Path path = java.createClasspath();
		path.append(antPath(project, nextPhase.classes().name()));
		for (Path cpItem : classes.classpathItems()) {
			path.append(antPath(project, cpItem.name()));
		}

		// The Java task is broken and doesn't work like Javac, so this hack is
		// needed:
		java.createJvmarg().setValue("-classpath");
		java.createJvmarg().setValue(path.toString());

		java.setClassname(WorkspaceBuilder.class.getCanonicalName());
		java.createArg().setValue(nextPhase.className());
		java.createArg().setValue(wsRoot);
		java.createArg().setValue(targetArg);
		java.createArg().setValue(cacheDir);
		java.setFork(true);
		// TODO test with a longer chain and generate unique names:
		File err = new File(cacheDir + "/nextPhase-err");
		File out = new File(cacheDir + "/nextPhase-out");
		java.setError(err);
		java.setOutput(out);

		java.execute();
		print(out, System.out);
		print(err, System.err);
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
