package net.sf.iwant.core;

import java.io.File;
import java.util.SortedSet;

public class WorkspaceBuilder {

	public static void main(String[] args) {
		if (args.length != 4) {
			throw new IllegalArgumentException(
					"Expected arguments wsdefclass wsroot target cache-dir");
		}
		Class wsDef = wsDefClassByName(args[0]);
		String wsRoot = args[1];
		String target = args[2];
		String cacheDir = args[3];
		Locations locations = new Locations(wsRoot, cacheDir);
		if ("list-of/targets".equals(target)) {
			listOfTargets(wsDef, locations);
		} else {
			buildConstantFile(locations, target, wsDef);
		}
	}

	private static void buildConstantFile(Locations locations,
			String targetPath, Class wsDef) {
		// TODO robust parsing
		String targetName = targetPath.substring(targetPath.indexOf("/") + 1,
				targetPath.lastIndexOf("/"));
		try {
			Target target = target(wsDef, targetName, locations);
			ensureParentDirFor(locations.targetCacheDir() + "/"
					+ target.nameWithoutCacheDir());
			ensureParentDirFor(locations.contentDescriptionCacheDir() + "/"
					+ target.nameWithoutCacheDir());
			Refresher.forReal(locations).refresh(target);
			System.out.println(target.name());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	private static Target target(Class wsDefClass, String targetName,
			Locations locations) throws Exception {
		ContainerPath wsRoot = wsRoot(wsDefClass, locations);
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

	private static void listOfTargets(Class wsDefClass, Locations locations) {
		ContainerPath wsRoot = wsRoot(wsDefClass, locations);
		SortedSet<Target> targets = PathDigger.targets(wsRoot);
		for (Target target : targets) {
			System.out.println(target.nameWithoutCacheDir());
		}
	}

}
