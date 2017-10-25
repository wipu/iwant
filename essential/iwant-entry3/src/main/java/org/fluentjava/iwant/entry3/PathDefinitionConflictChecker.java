package org.fluentjava.iwant.entry3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;

class PathDefinitionConflictChecker {

	static void failIfConflictingPathDefinitions(List<? extends Path> paths) {
		Map<String, Path> seen = new HashMap<>();
		for (Path path : paths) {
			failIfConflictingPathDefinitions(seen, path);
		}
	}

	private static void failIfConflictingPathDefinitions(Map<String, Path> seen,
			Path path) {
		if (path == null) {
			throw new Iwant.IwantException("Null Path");
		}
		if (path.name() == null) {
			throw new Iwant.IwantException(
					"A Path of " + path.getClass() + " has null name.");
		}
		Path seenPath = seen.get(path.name());
		if (seenPath == path) {
			return;
		}
		if (seenPath == null) {
			seen.put(path.name(), path);
		} else {
			// shallow check
			String errorMessage = definitionConflictErrorMessage(path,
					seenPath);
			if (errorMessage != null) {
				throw newConflictException(path, errorMessage);
			}
		}
		for (Path ingredient : path.ingredients()) {
			failIfConflictingPathDefinitions(seen, ingredient);
		}
	}

	private static IwantException newConflictException(Path path,
			String errorMessage) {
		return new Iwant.IwantException(
				"Two conflicting definitions for Path name " + path.name()
						+ ":\n" + errorMessage);
	}

	private static String definitionConflictErrorMessage(Path p1, Path p2) {
		if (!p1.getClass().equals(p2.getClass())) {
			return "One is of\n " + p1.getClass() + "\nand another is of\n "
					+ p2.getClass();
		}

		if (p1 instanceof Target) {
			Target t1 = (Target) p1;
			// p2 is also Target, we checked classes above
			Target t2 = (Target) p2;
			String descr1 = t1.contentDescriptor();
			String descr2 = t2.contentDescriptor();
			if (!descr1.equals(descr2)) {
				return "One has content descriptor:\n" + descr1
						+ "\nand another:\n" + descr2;
			}
		}

		List<Path> ingr1 = p1.ingredients();
		List<Path> ingr2 = p2.ingredients();
		if (doIngredientsDiffer(ingr1, ingr2)) {
			return "One definition has ingredients:\n" + asLines(ingr1)
					+ "\nwhile another has:\n" + asLines(ingr2);
		}
		return null;
	}

	private static boolean doIngredientsDiffer(List<Path> ingr1,
			List<Path> ingr2) {
		if (ingr1.size() != ingr2.size()) {
			return true;
		}
		for (int i = 0; i < ingr1.size(); i++) {
			Path p1 = ingr1.get(i);
			Path p2 = ingr2.get(i);
			if (p1 == null || p2 == null) {
				return true;
			}
			if (!p1.name().equals(p2.name())) {
				return true;
			}
		}
		return false;
	}

	private static String asLines(List<Path> paths) {
		StringBuilder b = new StringBuilder();
		for (Path path : paths) {
			String name = path == null ? "null" : "'" + path.name() + "'";
			b.append(" ").append(name).append("\n");
		}
		return b.toString();
	}

}
