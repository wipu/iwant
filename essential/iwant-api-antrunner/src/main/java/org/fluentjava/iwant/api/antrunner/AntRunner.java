package org.fluentjava.iwant.api.antrunner;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.ExitCalledException;

public class AntRunner {

	public static void runAnt(List<File> antJars, File cachedScript,
			String... antArgs) throws Exception {
		final String className = "org.apache.tools.ant.Main";
		List<String> allArgs = new ArrayList<>();
		allArgs.add("-f");
		allArgs.add(cachedScript.getAbsolutePath());
		allArgs.addAll(Arrays.asList(antArgs));
		try {
			Iwant.runJavaMain(true, false, className, antJars,
					allArgs.toArray(new String[0]));
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof ExitCalledException) {
				ExitCalledException ece = (ExitCalledException) e.getCause();
				if (ece.status() != 0) {
					throw ece;
				} else {
					// ant just feels like trying to kill our JVM,
					// it was still a success
				}
			} else {
				throw asRuntimeException(e.getCause());
			}
		}
	}

	private static RuntimeException asRuntimeException(Throwable e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

}
