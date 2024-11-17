package org.fluentjava.iwant.entry3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.model.Target;
import org.junit.jupiter.api.Test;

public class TargetNameCheckerMicrobenchmarkTest {

	private static final int LEVEL_COUNT = 10;
	private static final int TARGETS_PER_LEVEL = 100;

	@Test
	public void checkABigTree() {
		System.err.println("Creating target tree");
		List<Target> targets = new ArrayList<>();
		Map<String, Target> targetCache = new HashMap<>();
		for (int i = 0; i < TARGETS_PER_LEVEL; i++) {
			Target t = target(targetCache, 0, i);
			targets.add(t);
		}
		System.err
				.println("Done creating target tree, checking target names...");
		long t1 = System.currentTimeMillis();
		TargetNameChecker.check(targets);
		long t2 = System.currentTimeMillis();
		System.err.println("Checking target names took " + (t2 - t1) + "ms.");
	}

	private static Target target(Map<String, Target> targetCache, int level,
			int index) {
		String name = "t-" + level + "-" + index;
		Target cached = targetCache.get(name);
		if (cached != null) {
			return cached;
		}
		ConcatenatedBuilder t = Concatenated.named(name);
		if (level < LEVEL_COUNT) {
			for (int i = 0; i < TARGETS_PER_LEVEL; i++) {
				Target dep = target(targetCache, level + 1, i);
				t.unixPathTo(dep);
			}
		}
		Concatenated target = t.end();
		targetCache.put(name, target);
		return target;
	}

}
