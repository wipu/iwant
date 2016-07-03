package net.sf.iwant.entry3;

import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;

class TargetNameChecker {

	public static void check(List<? extends Target> targets) {
		for (Target target : targets) {
			check(target);
		}
	}

	private static void check(Path path) {
		check(path.name());
		for (Path ingredient : path.ingredients()) {
			check(ingredient);
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
