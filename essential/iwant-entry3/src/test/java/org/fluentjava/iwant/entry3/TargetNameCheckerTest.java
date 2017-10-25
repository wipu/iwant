package org.fluentjava.iwant.entry3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;

public class TargetNameCheckerTest {

	private static Target target(String name, Path... deps) {
		ConcatenatedBuilder target = Concatenated.named(name);
		for (Path dep : deps) {
			target.contentOf(dep);
		}
		return target.end();
	}

	private static void validCases(Target... targets) {
		TargetNameChecker.check(Arrays.asList(targets));
	}

	private static void invalidCases(String errorMessage, Target... targets) {
		try {
			TargetNameChecker.check(Arrays.asList(targets));
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(errorMessage, e.getMessage());
		}
	}

	@Test
	public void validTargetNames() {
		validCases(target("a"), target("b/c"));
	}

	@Test
	public void doubleColonIsInvalidInTargetName() {
		invalidCases(
				"Name contains double colon (breaks TargetImplementedInBash): a::b",
				target("a::b"));
	}

	@Test
	public void doubleColonIsInvalidInIngredientName() {
		invalidCases(
				"Name contains double colon (breaks TargetImplementedInBash): b::c",
				target("a", Source.underWsroot("b::c")));
	}

}
