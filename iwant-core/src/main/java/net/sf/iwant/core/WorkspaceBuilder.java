package net.sf.iwant.core;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;

public class WorkspaceBuilder {

	public static void main(String[] args) {
		if (args.length != 4) {
			throw new IllegalArgumentException(
					"Expected arguments wsdefclass iwant-dir target cache-dir");
		}
		// EmptyWorkspace.class.getCanonicalName(), iwantRoot,
		// "list-of/targets", cacheDir });
		Class wsDef = wsDefClassByName(args[0]);
		String target = args[2];
		String cacheDir = args[3];
		if ("list-of/targets".equals(target)) {
			listOfTargets(wsDef);
		} else {
			buildConstantFile(cacheDir, target, wsDef);
		}
	}

	private static void buildConstantFile(String cacheDir, String targetPath,
			Class wsDef) {
		// TODO robust parsing
		String methodName = targetPath.substring(targetPath.indexOf("/") + 1,
				targetPath.lastIndexOf("/"));
		try {
			Target target = target(wsDef, methodName);
			Constant constant = (Constant) target.content();
			System.out.println("iwant/cached/example/target/" + target.name());
			String p = cacheDir;
			// TODO cache should be created by to-use-iwant-on.sh
			new File(p).mkdir();
			p += "/target";
			new File(p).mkdir();
			p += "/" + target.name();
			new FileWriter(p).append(constant.value()).close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Target target(Class wsDef, String methodName)
			throws Exception {
		Method method = wsDef.getMethod(methodName);
		Target target = (Target) method.invoke(wsDef.newInstance());
		return target;
	}

	private static Class wsDefClassByName(String wsDefName) {
		try {
			return Class.forName(wsDefName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static void listOfTargets(Class wsDef) {
		for (Method method : wsDef.getMethods()) {
			if (isTargetMethod(method))
				System.out.println(method.getName());
		}
	}

	private static boolean isTargetMethod(Method method) {
		return Target.class.isAssignableFrom(method.getReturnType());
	}

}
