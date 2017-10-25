package net.sf.iwant.iwantwsrootfinder;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

public class IwantWsRootFinderTest extends TestCase {

	public void testKnownFileIsFoundUnderWsRoot() throws IOException {
		File wsInfo = new File(IwantWsRootFinder.essential().getParentFile(),
				"as-iwant-developer/i-have/conf/ws-info");

		assertTrue(wsInfo.exists());
		assertTrue(
				FileUtils.readFileToString(wsInfo).contains("WSNAME=iwant\n"));
	}

	public void testKnownFileIsFoundUnderMockEssential() throws IOException {
		File mockedApiWsdef = new File(IwantWsRootFinder.mockEssential(),
				"iwant-api-wsdef/src/main/java/"
						+ "net/sf/iwant/api/wsdef/MockedApiWsdef.java");

		assertTrue(mockedApiWsdef.exists());
		assertTrue(FileUtils.readFileToString(mockedApiWsdef)
				.contains("public class MockedApiWsdef"));
	}

}
