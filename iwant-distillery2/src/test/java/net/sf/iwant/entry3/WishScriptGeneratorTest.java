package net.sf.iwant.entry3;

import junit.framework.TestCase;

public class WishScriptGeneratorTest extends TestCase {

	public void testListOfTargets() {
		assertEquals("#!/bin/bash\n" + "HERE=$(dirname \"$0\")\n"
				+ "exec \"$HERE/../help.sh\" \"list-of/targets\"\n",
				WishScriptGenerator.wishScriptContent("list-of/targets"));
	}

	public void testTargetHelloAsPath() {
		assertEquals("#!/bin/bash\n" + "HERE=$(dirname \"$0\")\n"
				+ "exec \"$HERE/../../help.sh\" \"target/hello/as-path\"\n",
				WishScriptGenerator.wishScriptContent("target/hello/as-path"));
	}

}
