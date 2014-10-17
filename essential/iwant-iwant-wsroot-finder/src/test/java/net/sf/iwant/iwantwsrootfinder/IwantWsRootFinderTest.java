package net.sf.iwant.iwantwsrootfinder;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

public class IwantWsRootFinderTest extends TestCase {

	public void testKnownFileIsFoundUnderWsRoot() throws IOException {
		File wsInfo = new File(IwantWsRootFinder.wsRoot(),
				"as-iwant-developer/i-have/conf/ws-info");

		assertTrue(wsInfo.exists());
		assertTrue(FileUtils.readFileToString(wsInfo)
				.contains("WSNAME=iwant\n"));
	}

	public void testKnownFileIsFoundUnderMockWsRoot() throws IOException {
		File mockedApiWsdef = new File(IwantWsRootFinder.mockWsRoot(),
				"essential/iwant-api-wsdef/src/main/java/"
						+ "net/sf/iwant/api/wsdef/MockedApiWsdef.java");

		assertTrue(mockedApiWsdef.exists());
		assertTrue(FileUtils.readFileToString(mockedApiWsdef).contains(
				"public class MockedApiWsdef"));
	}

}
