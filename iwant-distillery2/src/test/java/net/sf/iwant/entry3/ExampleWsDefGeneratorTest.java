package net.sf.iwant.entry3;

import java.io.IOException;

import junit.framework.TestCase;

public class ExampleWsDefGeneratorTest extends TestCase {

	private StringBuilder expectedWsdefdefStart;
	private StringBuilder expectedWsdefStart;
	private IwantEntry3TestArea testArea;

	@Override
	public void setUp() {
		expectedWsdefdefStart = new StringBuilder();
		expectedWsdefStart = new StringBuilder();
		testArea = new IwantEntry3TestArea();
	}

	private void assertChangeWsdefdefTo(String newPackage, String newName,
			String wsDefSrc) throws IOException {
		StringBuilder b = new StringBuilder();
		b.append("package com.example.wsdef;\n");
		b.append("public class WorkspaceProvider implements IwantWorkspaceProvider {\n");
		b.append("	private static Source workspaceSrc() {\n");
		b.append("		return Source.underWsroot(\"AS_EXAMPLE_DEVELOPER/i-have/wsdef\");\n");
		b.append("	}\n");
		b.append("}\n");

		testArea.hasFile("iwant-example-wsdef/src/main/java/"
				+ "com/example/wsdefdef/WorkspaceProvider.java", b.toString());

		String actualWsdefdef = ExampleWsDefGenerator.exampleWsdefdef(
				testArea.root(), newPackage, newName, wsDefSrc);

		assertTrue("actual:\n" + actualWsdefdef,
				actualWsdefdef.startsWith(expectedWsdefdefStart.toString()));
		assertTrue("actual:\n" + actualWsdefdef,
				actualWsdefdef.endsWith("\n}\n"));
	}

	private void assertChangeWsdefTo(String newPackage, String newName)
			throws IOException {
		testArea.hasFile(
				"iwant-example-wsdef/src/main/java/"
						+ "com/example/wsdef/Workspace.java",
				"package com.example.wsdef;\n"
						+ "public class Workspace implements IwantWorkspace {\n}\n");

		String actualWsdef = ExampleWsDefGenerator.exampleWsdef(
				testArea.root(), newPackage, newName);

		assertTrue("actual:\n" + actualWsdef,
				actualWsdef.startsWith(expectedWsdefStart.toString()));
		assertTrue("actual:\n" + actualWsdef, actualWsdef.endsWith("\n}\n"));
	}

	public void testWsdefdefName1() throws IOException {
		expectedWsdefdefStart.append("package new.package1;\n");
		expectedWsdefdefStart
				.append("public class Ws1 implements IwantWorkspaceProvider {\n");
		expectedWsdefdefStart
				.append("	private static Source workspaceSrc() {\n");
		expectedWsdefdefStart
				.append("		return Source.underWsroot(\"as-x1/i-have/wsdef\");\n");
		expectedWsdefdefStart.append("	}\n");
		expectedWsdefdefStart.append("}\n");

		assertChangeWsdefdefTo("new.package1", "Ws1", "as-x1/i-have/wsdef");
	}

	public void testWsdefdefName2() throws IOException {
		expectedWsdefdefStart.append("package new.package2;\n");
		expectedWsdefdefStart
				.append("public class Ws2 implements IwantWorkspaceProvider {\n");
		expectedWsdefdefStart
				.append("	private static Source workspaceSrc() {\n");
		expectedWsdefdefStart
				.append("		return Source.underWsroot(\"as-x2/i-have/wsdef\");\n");
		expectedWsdefdefStart.append("	}\n");
		expectedWsdefdefStart.append("}\n");

		assertChangeWsdefdefTo("new.package2", "Ws2", "as-x2/i-have/wsdef");
	}

	public void testWsdefName1() throws IOException {
		expectedWsdefStart.append("package new.package1;\n");
		expectedWsdefStart
				.append("public class Ws1 implements IwantWorkspace ");

		assertChangeWsdefTo("new.package1", "Ws1");
	}

	public void testWsdefName2() throws IOException {
		expectedWsdefStart.append("package new.package2;\n");
		expectedWsdefStart
				.append("public class Ws2 implements IwantWorkspace ");

		assertChangeWsdefTo("new.package2", "Ws2");
	}

}
