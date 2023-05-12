package org.fluentjava.iwant.entry3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Target;

class TargetNameChecker {

	public static void check(List<? extends Target> targets) {
		Set<String> seen = new HashSet<>();
		for (Target target : targets) {
			check(target, seen);
		}
	}

	private static void check(Path path, Set<String> seen) {
		if (seen.contains(path.name())) {
			return;
		}
		check(path.name());
		seen.add(path.name());
		for (Path ingredient : path.ingredients()) {
			check(ingredient, seen);
		}
	}

	private static void check(String name) {
		if (name.contains("::")) {
			throw new IllegalArgumentException(
					"Name contains double colon (breaks TargetImplementedInBash): "
							+ name);
		}
	}

}
