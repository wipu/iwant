package net.sf.iwant.entry3;

import junit.framework.TestCase;

public class ExampleWsDefGeneratorTest extends TestCase {

	private StringBuilder expectedStart;
	private IwantEntry3TestArea testArea;

	public void setUp() {
		expectedStart = new StringBuilder();
		testArea = new IwantEntry3TestArea();
	}

	private void assertChangeTo(String newPackage, String newName) {
		testArea.hasFile("iwant-example-wsdef/src/main/java/"
				+ "com/example/wsdef/Workspace.java",
				"package com.example.wsdef;\n"
						+ "public class Workspace {\n}\n");
		String actualOut = ExampleWsDefGenerator.example(testArea.root(),
				newPackage, newName);
		assertTrue("actual:\n" + actualOut,
				actualOut.startsWith(expectedStart.toString()));
		assertTrue("actual:\n" + actualOut, actualOut.endsWith("\n}\n"));
	}

	public void testName1() {
		expectedStart.append("package new.package1;\n");
		expectedStart.append("public class Ws1 ");

		assertChangeTo("new.package1", "Ws1");
	}

	public void testName2() {
		expectedStart.append("package new.package2;\n");
		expectedStart.append("public class Ws2 ");

		assertChangeTo("new.package2", "Ws2");
	}

}
