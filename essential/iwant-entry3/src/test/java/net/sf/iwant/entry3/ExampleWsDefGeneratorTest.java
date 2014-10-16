package net.sf.iwant.entry3;

import junit.framework.TestCase;
import net.sf.iwant.testarea.TestArea;

public class ExampleWsDefGeneratorTest extends TestCase {

	private StringBuilder expectedWsdefdefStart;
	private StringBuilder expectedWsdefStart;
	private TestArea testArea;

	@Override
	public void setUp() {
		expectedWsdefdefStart = new StringBuilder();
		expectedWsdefStart = new StringBuilder();
		testArea = TestArea.forTest(this);
	}

	private void assertChangeWsdefdefTo(String newPackage, String newName,
			String wsDefSrc) {
		StringBuilder b = new StringBuilder();
		b.append("package com.example.wsdef;\n");
		b.append("public class WorkspaceProvider implements IwantWorkspaceProvider {\n");
		b.append("	public JavaModule workspaceModule(JavaModule iwantApiClasses) {\n");
		b.append("		return JavaSrcModule.with().name(\"WSNAME-workspace\")\n");
		b.append("				.locationUnderWsRoot(\"as-WSNAME-developer/i-have/wsdef\")\n");
		b.append("				.mainJava(\"src/main/java\").mainDeps(iwantApiClasses).end();\n");
		b.append("	}\n");
		b.append("}\n");

		testArea.hasFile("private/iwant-example-wsdef/src/main/java/"
				+ "com/example/wsdefdef/WorkspaceProvider.java", b.toString());

		String actualWsdefdef = ExampleWsDefGenerator.exampleWsdefdef(
				testArea.root(), newPackage, newName, wsDefSrc);

		assertTrue("actual:\n" + actualWsdefdef,
				actualWsdefdef.startsWith(expectedWsdefdefStart.toString()));
		assertTrue("actual:\n" + actualWsdefdef,
				actualWsdefdef.endsWith("\n}\n"));
	}

	private void assertChangeWsdefTo(String newPackage, String newName) {
		testArea.hasFile(
				"private/iwant-example-wsdef/src/main/java/"
						+ "com/example/wsdef/Workspace.java",
				"package com.example.wsdef;\n"
						+ "public class Workspace implements IwantWorkspace {\n}\n");

		String actualWsdef = ExampleWsDefGenerator.exampleWsdef(
				testArea.root(), newPackage, newName);

		assertTrue("actual:\n" + actualWsdef,
				actualWsdef.startsWith(expectedWsdefStart.toString()));
		assertTrue("actual:\n" + actualWsdef, actualWsdef.endsWith("\n}\n"));
	}

	public void testWsdefdefName1() {
		expectedWsdefdefStart.append("package new.package1;\n");
		expectedWsdefdefStart
				.append("public class Ws1 implements IwantWorkspaceProvider {\n");
		expectedWsdefdefStart
				.append("	public JavaModule workspaceModule(JavaModule iwantApiClasses) {\n");
		expectedWsdefdefStart
				.append("		return JavaSrcModule.with().name(\"x1-workspace\")\n");
		expectedWsdefdefStart
				.append("				.locationUnderWsRoot(\"as-x1-developer/i-have/wsdef\")\n");

		assertChangeWsdefdefTo("new.package1", "Ws1", "x1");
	}

	public void testWsdefdefName2() {
		expectedWsdefdefStart.append("package new.package2;\n");
		expectedWsdefdefStart
				.append("public class Ws2 implements IwantWorkspaceProvider {\n");
		expectedWsdefdefStart
				.append("	public JavaModule workspaceModule(JavaModule iwantApiClasses) {\n");
		expectedWsdefdefStart
				.append("		return JavaSrcModule.with().name(\"x2-workspace\")\n");
		expectedWsdefdefStart
				.append("				.locationUnderWsRoot(\"as-x2-developer/i-have/wsdef\")\n");

		assertChangeWsdefdefTo("new.package2", "Ws2", "x2");
	}

	public void testWsdefName1() {
		expectedWsdefStart.append("package new.package1;\n");
		expectedWsdefStart
				.append("public class Ws1 implements IwantWorkspace ");

		assertChangeWsdefTo("new.package1", "Ws1");
	}

	public void testWsdefName2() {
		expectedWsdefStart.append("package new.package2;\n");
		expectedWsdefStart
				.append("public class Ws2 implements IwantWorkspace ");

		assertChangeWsdefTo("new.package2", "Ws2");
	}

}
