package net.sf.iwant.api.bash;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class IndentedStringArrayParserTest {

	private static void assertCase(String actualToString, List<String> lines) {
		List<String[]> list = TargetImplementedInBash
				.parseIndentedStringArrays(lines);
		StringBuilder b = new StringBuilder();
		for (String[] a : list) {
			b.append(Arrays.toString(a));
		}

		assertEquals(actualToString, b.toString());
	}

	@Test
	public void parseSimple() {
		List<String> lines = Arrays.asList(": a0v0", ": a0v1", "::", ": a1v0",
				": a1v1", "::");

		assertCase("[a0v0, a0v1][a1v0, a1v1]", lines);
	}

	@Test
	public void parseMultiLined() {
		List<String> lines = Arrays.asList(": a0v0l0", " a0v0l1", ": a0v1",
				": a0v2l0", " a0v2l1", "::", ": a1v0", "::");

		assertCase("[a0v0l0\n" + "a0v0l1, a0v1, a0v2l0\n" + "a0v2l1][a1v0]",
				lines);
	}

	@Test
	public void parseIndentedMultiLined() {
		List<String> lines = Arrays.asList(":  a0v0l0", "  a0v0l1", ":  a0v1",
				":  a0v2l0", "  a0v2l1", "::", ":  a1v0", "::");

		assertCase(
				"[ a0v0l0\n" + " a0v0l1,  a0v1,  a0v2l0\n" + " a0v2l1][ a1v0]",
				lines);
	}

}
