package net.sf.iwant.core;

import java.io.File;
import java.lang.reflect.Method;

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
		String methodName = targetPath.substring(targetPath.indexOf("/") + 1,
				targetPath.lastIndexOf("/"));
		try {
			Target target = target(wsDef, methodName, locations);
			// TODO cache should be created by to-use-iwant-on.sh
			ensureCacheDir(new File(locations.targetCacheDir()));
			ensureCacheDir(new File(locations.contentDescriptionCacheDir()));
			Refresher.forReal(locations).refresh(target);
			System.out.println(target.name());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void ensureCacheDir(File cacheDir) {
		File parent = cacheDir.getParentFile();
		if (!parent.exists())
			ensureCacheDir(parent);
		if (!cacheDir.exists())
			cacheDir.mkdir();
	}

	private static Target target(Class wsDefClass, String methodName,
			Locations locations) throws Exception {
		ContainerPath wsRoot = wsRoot(wsDefClass, locations);
		Method method = wsRoot.getClass().getMethod(methodName);
		Target target = (Target) method.invoke(wsRoot);
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
		for (Method method : wsRoot.getClass().getMethods()) {
			if (isTargetMethod(method))
				System.out.println(method.getName());
		}
	}

	private static boolean isTargetMethod(Method method) {
		return Target.class.isAssignableFrom(method.getReturnType());
	}

}
