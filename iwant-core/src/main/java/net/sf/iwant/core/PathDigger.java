package net.sf.iwant.core;

import java.lang.reflect.Method;
import java.util.SortedSet;
import java.util.TreeSet;

class PathDigger {

	public static SortedSet<Target> targets(ContainerPath container) {
		SortedSet<Target> targets = new TreeSet();
		for (Method method : container.getClass().getMethods()) {
			if (isTargetMethod(method)) {
				Target target = invokeTargetMethod(container, method);
				targets.add(target);
			}
		}
		return targets;
	}

	public static Target target(ContainerPath container, String targetName) {
		SortedSet<Target> targets = targets(container);
		for (Target target : targets) {
			if (targetName.equals(target.nameWithoutCacheDir())) {
				return target;
			}
		}
		throw new IllegalArgumentException("No such target: " + targetName);
	}

	private static Target invokeTargetMethod(ContainerPath container,
			Method method) {
		try {
			Target target = (Target) method.invoke(container);
			return target;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isTargetMethod(Method method) {
		if (method.getParameterTypes().length > 0) {
			return false;
		}
		return Target.class.isAssignableFrom(method.getReturnType());
	}

}
