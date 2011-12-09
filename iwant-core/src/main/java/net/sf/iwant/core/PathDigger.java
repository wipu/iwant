package net.sf.iwant.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

class PathDigger {

	public static SortedSet<Target<?>> targets(ContainerPath container) {
		SortedSet<Target<?>> targets = new TreeSet<Target<?>>();
		for (Method method : container.getClass().getMethods()) {
			if (isTargetMethod(method)) {
				Target<?> target = invokeTargetMethod(container, method);
				targets.add(target);
			}
		}
		return targets;
	}

	public static NextPhase nextPhase(ContainerPath container) {
		List<NextPhase> nextPhases = new ArrayList<NextPhase>();
		for (Method method : container.getClass().getMethods()) {
			if (isNextPhaseMethod(method)) {
				NextPhase nextPhase = invokeNextPhaseMethod(container, method);
				nextPhases.add(nextPhase);
			}
		}
		if (nextPhases.isEmpty()) {
			return null;
		}
		if (nextPhases.size() > 1) {
			throw new IllegalArgumentException(
					"More than one NextPhase defined!");
		}
		return nextPhases.get(0);
	}

	public static Target<?> target(ContainerPath container, String targetName) {
		SortedSet<Target<?>> targets = targets(container);
		return target(targets, targetName);
	}

	public static Target<?> target(SortedSet<Target<?>> targets,
			String targetName) {
		for (Target<?> target : targets) {
			if (targetName.equals(target.name())) {
				return target;
			}
		}
		return null;
	}

	private static Target<?> invokeTargetMethod(ContainerPath container,
			Method method) {
		try {
			Target<?> target = (Target<?>) method.invoke(container);
			return target;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static NextPhase invokeNextPhaseMethod(ContainerPath container,
			Method method) {
		try {
			NextPhase nextPhase = (NextPhase) method.invoke(container);
			return nextPhase;
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

	private static boolean isNextPhaseMethod(Method method) {
		if (method.getParameterTypes().length > 0) {
			return false;
		}
		return NextPhase.class.isAssignableFrom(method.getReturnType());
	}

}
