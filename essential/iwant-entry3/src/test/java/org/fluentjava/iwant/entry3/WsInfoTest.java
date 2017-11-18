package org.fluentjava.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.fluentjava.iwant.api.model.WsInfo;
import org.fluentjava.iwant.entry.Iwant.IwantException;

import junit.framework.TestCase;

public class WsInfoTest extends TestCase {

	private StringBuilder in;
	private File asSomeone;

	@Override
	public void setUp() {
		in = new StringBuilder();
		asSomeone = new File("/project/as-test");
	}

	private WsInfo newWsInfo() throws IOException {
		return new WsInfoFileImpl(new StringReader(in.toString()),
				new File("/project/as-test/i-have/conf/wsinfo"), asSomeone);
	}

	public void testEmpty() throws IOException {
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

	public void testMissingWsname() throws IOException {
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

	public void testMissingWsroot() throws IOException {
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

	public void testMissingWsdefdefModule() throws IOException {
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

	public void testMissingWsdefdefClass() throws IOException {
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

	public void testAsSomeoneOutsideWsrootIsAnError() throws IOException {
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

	public void testValid() throws IOException {
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

	public void testValidWithDifferentValues() throws IOException {
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
		assertEquals(
				"/project/wsroot/wsdefinition/src/main/java/"
						+ "com/example2/wsdefdef/Example2WorkspaceProvider.java",
				wsInfo.wsdefdefJava().toString());
		assertEquals("com.example2.wsdefdef", wsInfo.wsdefdefPackage());
		assertEquals("Example2WorkspaceProvider",
				wsInfo.wsdefdefClassSimpleName());
		assertEquals("as-test2", wsInfo.relativeAsSomeone());
	}

}
