package org.fluentjava.iwant.api.bash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class IngredientTypeNameValueTest {

	@Test
	public void parseSimple() {
		List<String> lines = Arrays.asList(": param", ": name 1", ": value 1",
				"::", ": target-dep", ": name 2", ": value 2", "::");

		assertEquals("[param|name 1|value 1, target-dep|name 2|value 2]",
				TargetImplementedInBash.parseIngredients(lines).toString());
	}

	@Test
	public void parseMultiLined() {
		List<String> lines = Arrays.asList(": param", ": param", " name",
				": param", " value", "::", ": target-dep", ": dep", " name",
				": dep", " value", "::");

		assertEquals(
				"[param|param\n" + "name|param\n" + "value, target-dep|dep\n"
						+ "name|dep\n" + "value]",
				TargetImplementedInBash.parseIngredients(lines).toString());
	}

	@Test
	public void parseIndentedMultiLined() {
		List<String> lines = Arrays.asList(": param", ":  param", "  name",
				":  param", "  value", "::", ": target-dep", ":  dep", "  name",
				":  dep", "  value", "::");

		assertEquals("[param| param\n" + " name| param\n"
				+ " value, target-dep| dep\n" + " name| dep\n" + " value]",
				TargetImplementedInBash.parseIngredients(lines).toString());
	}

}
