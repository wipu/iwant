package org.fluentjava.iwant.entry3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class WishScriptGeneratorTest {

	@Test
	public void listOfTargets() {
		assertEquals(
				"#!/bin/bash\n" + "HERE=$(dirname \"$0\")\n"
						+ "exec \"$HERE/../help.sh\" \"list-of/targets\"\n",
				WishScriptGenerator.wishScriptContent("list-of/targets"));
	}

	@Test
	public void targetHelloAsPath() {
		assertEquals("#!/bin/bash\n" + "HERE=$(dirname \"$0\")\n"
				+ "exec \"$HERE/../../help.sh\" \"target/hello/as-path\"\n",
				WishScriptGenerator.wishScriptContent("target/hello/as-path"));
	}

}
