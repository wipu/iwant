package org.fluentjava.iwant.entry3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExampleWsDefGeneratorTest {

	private StringBuilder expectedWsdefdefStart;
	private StringBuilder expectedWsdefStart;
	private TestArea testArea;

	@BeforeEach
	public void before() {
		expectedWsdefdefStart = new StringBuilder();
		expectedWsdefStart = new StringBuilder();
		testArea = TestArea.forTest(this);
	}

	private void assertChangeWsdefdefTo(String newPackage, String newName,
			String wsDefSrc, String wsdefClassName) {
		StringBuilder b = new StringBuilder();
		b.append("package com.example.wsdef;\n");
		b.append(
				"public class WorkspaceProvider implements WorkspaceModuleProvider {\n");
		b.append(
				"	public JavaModule workspaceModule(JavaModule iwantApiClasses) {\n");
		b.append("		return JavaSrcModule.with().name(\"WSNAME-wsdef\")\n");
		b.append(
				"				.locationUnderWsRoot(\"as-WSNAME-developer/i-have/wsdef\")\n");
		b.append(
				"				.mainJava(\"src/main/java\").mainDeps(iwantApiClasses).end();\n");
		b.append("	}\n");
		b.append("  public String workspaceClassname {\n");
		b.append("    return \"WSDEF\";\n");
		b.append("  }\n");
		b.append("}\n");

		testArea.hasFile(
				"iwant-example-wsdef/src/main/java/"
						+ "com/example/wsdefdef/WorkspaceProvider.java",
				b.toString());

		String actualWsdefdef = ExampleWsDefGenerator.exampleWsdefdef(
				testArea.root(), newPackage, newName, wsDefSrc, wsdefClassName);

		assertTrue(actualWsdefdef.startsWith(expectedWsdefdefStart.toString()),
				"actual:\n" + actualWsdefdef);
		assertTrue(actualWsdefdef.endsWith("\n}\n"),
				"actual:\n" + actualWsdefdef);
		assertTrue(
				actualWsdefdef
						.contains("return \"" + wsdefClassName + "Factory\";"),
				"actual:\n" + actualWsdefdef);
	}

	private void assertChangeWsdefTo(String newPackage, String newName) {
		testArea.hasFile(
				"iwant-example-wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWorkspaceFactory.java",
				"package com.example.wsdef;\n"
						+ "public class ExampleWorkspaceFactory implements WorkspaceFactory {\n"
						+ "	@Override\n"
						+ "	public Workspace workspace(WorkspaceContext ctx) {\n"
						+ "		return new ExampleWorkspace();\n" + "	}\n"
						+ "" + "}\n");

		String actualWsdef = ExampleWsDefGenerator.exampleWsdef(testArea.root(),
				newPackage, newName);

		assertTrue(actualWsdef.startsWith(expectedWsdefStart.toString()),
				"actual:\n" + actualWsdef);
		assertTrue(actualWsdef.contains("return new " + newName + "("),
				"actual:\n" + actualWsdef);
		assertTrue(actualWsdef.endsWith("\n}\n"), "actual:\n" + actualWsdef);
	}

	private void assertChangeWsTo(String newPackage, String newName) {
		testArea.hasFile(
				"iwant-example-wsdef/src/main/java/"
						+ "com/example/wsdef/ExampleWorkspace.java",
				"package com.example.wsdef;\n"
						+ "public class ExampleWorkspace implements Workspace {\n}\n");

		String actualWs = ExampleWsDefGenerator.exampleWs(testArea.root(),
				newPackage, newName);

		assertTrue(actualWs.startsWith(expectedWsdefStart.toString()),
				"actual:\n" + actualWs);
		assertTrue(actualWs.endsWith("\n}\n"), "actual:\n" + actualWs);
	}

	@Test
	public void wsdefdefName1() {
		expectedWsdefdefStart.append("package new.package1;\n");
		expectedWsdefdefStart.append(
				"public class Ws1 implements WorkspaceModuleProvider {\n");
		expectedWsdefdefStart.append(
				"	public JavaModule workspaceModule(JavaModule iwantApiClasses) {\n");
		expectedWsdefdefStart.append(
				"		return JavaSrcModule.with().name(\"x1-wsdef\")\n");
		expectedWsdefdefStart.append(
				"				.locationUnderWsRoot(\"as-x1-developer/i-have/wsdef\")\n");

		assertChangeWsdefdefTo("new.package1", "Ws1", "x1",
				"new.wsdefpack.Wsdef");
	}

	@Test
	public void wsdefdefName2() {
		expectedWsdefdefStart.append("package new.package2;\n");
		expectedWsdefdefStart.append(
				"public class Ws2 implements WorkspaceModuleProvider {\n");
		expectedWsdefdefStart.append(
				"	public JavaModule workspaceModule(JavaModule iwantApiClasses) {\n");
		expectedWsdefdefStart.append(
				"		return JavaSrcModule.with().name(\"x2-wsdef\")\n");
		expectedWsdefdefStart.append(
				"				.locationUnderWsRoot(\"as-x2-developer/i-have/wsdef\")\n");

		assertChangeWsdefdefTo("new.package2", "Ws2", "x2",
				"new.wsdefpak2.Wsdef2");
	}

	@Test
	public void wsdefName1() {
		expectedWsdefStart.append("package new.package1;\n");
		expectedWsdefStart
				.append("public class Ws1Factory implements WorkspaceFactory ");

		assertChangeWsdefTo("new.package1", "Ws1");
	}

	@Test
	public void wsdefName2() {
		expectedWsdefStart.append("package new.package2;\n");
		expectedWsdefStart
				.append("public class Ws2Factory implements WorkspaceFactory ");

		assertChangeWsdefTo("new.package2", "Ws2");
	}

	@Test
	public void wsName1() {
		expectedWsdefStart.append("package new.package1;\n");
		expectedWsdefStart.append("public class Ws1 implements Workspace ");

		assertChangeWsTo("new.package1", "Ws1");
	}

	@Test
	public void wsName2() {
		expectedWsdefStart.append("package new.package2;\n");
		expectedWsdefStart.append("public class Ws2 implements Workspace ");

		assertChangeWsTo("new.package2", "Ws2");
	}

	@Test
	public void proposedWsdefPackage() {
		assertEquals("com.example.project.wsdef", ExampleWsDefGenerator
				.proposedWsdefPackage("com.example.project.wsdefdef"));
		assertEquals("org.oikarinen.reuhu.wsdef", ExampleWsDefGenerator
				.proposedWsdefPackage("org.oikarinen.reuhu.wsdefdef"));
		assertEquals("net.esimerkki.nonwsdefdefpackage.wsdef",
				ExampleWsDefGenerator.proposedWsdefPackage(
						"net.esimerkki.nonwsdefdefpackage"));
	}

	@Test
	public void proposedWsdefSimpleName() {
		assertEquals("Ws0Workspace",
				ExampleWsDefGenerator.proposedWsdefSimpleName("ws0"));
		assertEquals("OthernameWorkspace",
				ExampleWsDefGenerator.proposedWsdefSimpleName("othername"));
		assertEquals("AWorkspace",
				ExampleWsDefGenerator.proposedWsdefSimpleName("a"));
		assertEquals("DashednameWorkspace",
				ExampleWsDefGenerator.proposedWsdefSimpleName("dashed-name"));
		assertEquals("AB0Workspace",
				ExampleWsDefGenerator.proposedWsdefSimpleName("aB0"));
		assertEquals("_0aWorkspace",
				ExampleWsDefGenerator.proposedWsdefSimpleName("0a"));
	}

}
