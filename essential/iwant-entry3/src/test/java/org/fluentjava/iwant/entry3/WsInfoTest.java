package org.fluentjava.iwant.entry3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.fluentjava.iwant.api.model.WsInfo;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WsInfoTest {

	private StringBuilder in;
	private File asSomeone;

	@BeforeEach
	public void before() {
		in = new StringBuilder();
		asSomeone = new File("/project/as-test");
	}

	private WsInfo newWsInfo() throws IOException {
		return new WsInfoFileImpl(new StringReader(in.toString()),
				new File("/project/as-test/i-have/conf/wsinfo"), asSomeone);
	}

	@Test
	public void empty() throws IOException {
		// empty in

		try {
			newWsInfo();
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Please specify WSNAME in /project/as-test/i-have/conf/wsinfo",
					e.getMessage());
		}

	}

	@Test
	public void missingWsname() throws IOException {
		in.append("WSROOT=../../..\n");
		in.append("WSDEFDEF_MODULE=../wsdef\n");
		in.append(
				"WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWorkspaceProvider\n");

		try {
			newWsInfo();
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Please specify WSNAME in /project/as-test/i-have/conf/wsinfo",
					e.getMessage());
		}

	}

	@Test
	public void missingWsroot() throws IOException {
		in.append("WSNAME=example\n");
		in.append("WSDEFDEF_MODULE=../wsdef\n");
		in.append(
				"WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWorkspaceProvider\n");

		try {
			newWsInfo();
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Please specify WSROOT in /project/as-test/i-have/conf/wsinfo",
					e.getMessage());
		}

	}

	@Test
	public void missingWsdefdefModule() throws IOException {
		in.append("WSNAME=example\n");
		in.append("WSROOT=../../..\n");
		in.append(
				"WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWorkspaceProvider\n");

		try {
			newWsInfo();
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Please specify WSDEFDEF_MODULE in /project/as-test/i-have/conf/wsinfo",
					e.getMessage());
		}

	}

	@Test
	public void missingWsdefdefClass() throws IOException {
		in.append("WSNAME=example\n");
		in.append("WSROOT=../../..\n");
		in.append("WSDEFDEF_MODULE=../wsdef\n");

		try {
			newWsInfo();
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Please specify WSDEFDEF_CLASS in /project/as-test/i-have/conf/wsinfo",
					e.getMessage());
		}

	}

	@Test
	public void asSomeoneOutsideWsrootIsAnError() throws IOException {
		in.append("WSNAME=example\n");
		in.append("WSROOT=../../..\n");
		in.append("WSDEFDEF_MODULE=../wsdef\n");
		in.append(
				"WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWorkspaceProvider\n");
		asSomeone = new File("/different-project/as-test");

		try {
			newWsInfo();
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(
					"/different-project/as-test is not a child of /project",
					e.getMessage());
		}
	}

	@Test
	public void valid() throws IOException {
		in.append("WSNAME=example\n");
		in.append("WSROOT=../../..\n");
		in.append("WSDEFDEF_MODULE=../wsdef\n");
		in.append(
				"WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWorkspaceProvider\n");

		WsInfo wsInfo = newWsInfo();

		assertEquals("example", wsInfo.wsName());
		assertEquals("/project", wsInfo.wsRoot().toString());
		assertEquals("/project/as-test/i-have/wsdef",
				wsInfo.wsdefdefModule().getCanonicalPath());
		assertEquals("/project/as-test/i-have/wsdef/src/main/java",
				wsInfo.wsdefdefSrc().getCanonicalPath());
		assertEquals("com.example.wsdefdef.ExampleWorkspaceProvider",
				wsInfo.wsdefdefClass());
		assertEquals(
				"/project/as-test/i-have/wsdef/src/main/java/"
						+ "com/example/wsdefdef/ExampleWorkspaceProvider.java",
				wsInfo.wsdefdefJava().toString());
		assertEquals("com.example.wsdefdef", wsInfo.wsdefdefPackage());
		assertEquals("ExampleWorkspaceProvider",
				wsInfo.wsdefdefClassSimpleName());
		assertEquals("as-test", wsInfo.relativeAsSomeone());
	}

	@Test
	public void validWithDifferentValues() throws IOException {
		asSomeone = new File("/project/wsroot/as-test2");
		in.append("WSNAME=example2\n");
		in.append("WSROOT=../../../wsroot\n");
		in.append("WSDEFDEF_MODULE=../../../wsroot/wsdefinition\n");
		in.append(
				"WSDEFDEF_CLASS=com.example2.wsdefdef.Example2WorkspaceProvider\n");

		WsInfo wsInfo = newWsInfo();

		assertEquals("example2", wsInfo.wsName());
		assertEquals("/project/wsroot", wsInfo.wsRoot().getCanonicalPath());
		assertEquals("/project/wsroot/wsdefinition",
				wsInfo.wsdefdefModule().toString());
		assertEquals("/project/wsroot/wsdefinition/src/main/java",
				wsInfo.wsdefdefSrc().toString());
		assertEquals("com.example2.wsdefdef.Example2WorkspaceProvider",
				wsInfo.wsdefdefClass());
		assertEquals("/project/wsroot/wsdefinition/src/main/java/"
				+ "com/example2/wsdefdef/Example2WorkspaceProvider.java",
				wsInfo.wsdefdefJava().toString());
		assertEquals("com.example2.wsdefdef", wsInfo.wsdefdefPackage());
		assertEquals("Example2WorkspaceProvider",
				wsInfo.wsdefdefClassSimpleName());
		assertEquals("as-test2", wsInfo.relativeAsSomeone());
	}

}
