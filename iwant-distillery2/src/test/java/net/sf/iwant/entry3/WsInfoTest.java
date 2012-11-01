package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;
import net.sf.iwant.api.WsInfo;
import net.sf.iwant.entry.Iwant.IwantException;

public class WsInfoTest extends TestCase {

	private StringBuilder in;

	@Override
	public void setUp() {
		in = new StringBuilder();
	}

	private WsInfo newWsInfo() throws IOException {
		return new WsInfoFileImpl(new StringReader(in.toString()), new File(
				"/project/as-test/i-have/conf/wsinfo"));
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
		in.append("WSDEF_SRC=../wsdef/src/main/java\n");
		in.append("WSDEF_CLASS=com.example.wsdef.Workspace\n");

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
		in.append("WSDEF_SRC=../wsdef/src/main/java\n");
		in.append("WSDEF_CLASS=com.example.wsdef.Workspace\n");

		try {
			newWsInfo();
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Please specify WSROOT in /project/as-test/i-have/conf/wsinfo",
					e.getMessage());
		}

	}

	public void testMissingWsdefSrc() throws IOException {
		in.append("WSNAME=example\n");
		in.append("WSROOT=../../..\n");
		in.append("WSDEF_CLASS=com.example.wsdef.Workspace\n");

		try {
			newWsInfo();
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Please specify WSDEF_SRC in /project/as-test/i-have/conf/wsinfo",
					e.getMessage());
		}

	}

	public void testMissingWsdefClass() throws IOException {
		in.append("WSNAME=example\n");
		in.append("WSROOT=../../..\n");
		in.append("WSDEF_SRC=../wsdef/src/main/java\n");

		try {
			newWsInfo();
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Please specify WSDEF_CLASS in /project/as-test/i-have/conf/wsinfo",
					e.getMessage());
		}

	}

	public void testValid() throws IOException {
		in.append("WSNAME=example\n");
		in.append("WSROOT=../../..\n");
		in.append("WSDEF_SRC=../wsdef/src/main/java\n");
		in.append("WSDEF_CLASS=com.example.wsdef.Workspace\n");

		WsInfo wsInfo = newWsInfo();

		assertEquals("example", wsInfo.wsName());
		assertEquals("/project", wsInfo.wsRoot().toString());
		assertEquals("/project/as-test/i-have/wsdef/src/main/java", wsInfo
				.wsdefdefSrc().getCanonicalPath());
		assertEquals("com.example.wsdef.Workspace", wsInfo.wsdefClass());
		assertEquals("/project/as-test/i-have/wsdef/src/main/java/"
				+ "com/example/wsdef/Workspace.java", wsInfo.wsdefdefJava()
				.toString());
		assertEquals("com.example.wsdef", wsInfo.wsdefdefPackage());
		assertEquals("Workspace", wsInfo.wsdefdefClassSimpleName());
	}

	public void testValidWithDifferentValues() throws IOException {
		in.append("WSNAME=example2\n");
		in.append("WSROOT=../../../wsroot\n");
		in.append("WSDEF_SRC=../../../wsroot/wsdefinition\n");
		in.append("WSDEF_CLASS=com.example2.wsdef.Workspace2\n");

		WsInfo wsInfo = newWsInfo();

		assertEquals("example2", wsInfo.wsName());
		assertEquals("/project/wsroot", wsInfo.wsRoot().getCanonicalPath());
		assertEquals("/project/wsroot/wsdefinition", wsInfo.wsdefdefSrc()
				.toString());
		assertEquals("com.example2.wsdef.Workspace2", wsInfo.wsdefClass());
		assertEquals(
				"/project/wsroot/wsdefinition/com/example2/wsdef/Workspace2.java",
				wsInfo.wsdefdefJava().toString());
		assertEquals("com.example2.wsdef", wsInfo.wsdefdefPackage());
		assertEquals("Workspace2", wsInfo.wsdefdefClassSimpleName());
	}

}
