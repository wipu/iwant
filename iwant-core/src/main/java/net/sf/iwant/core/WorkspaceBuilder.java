package net.sf.iwant.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
			buildMockConstantFile(cacheDir);
		}
	}

	private static void buildMockConstantFile(String cacheDir) {
		System.out.println("iwant/cached/example/target/aConstant");
		try {
			String p = cacheDir;
			// TODO cache should be created by to-use-iwant-on.sh
			new File(p).mkdir();
			p += "/target";
			new File(p).mkdir();
			p += "/aConstant";
			new FileWriter(p).append("Constant generated content\n").close();
		} catch (IOException e) {
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

	private static void listOfTargets(Class wsDef) {
		for (Method method : wsDef.getMethods()) {
			if (isTargetMethod(method))
				System.out.println(method.getName());
		}
	}

	private static boolean isTargetMethod(Method method) {
		return Path.class.isAssignableFrom(method.getReturnType());
	}

}
