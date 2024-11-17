package org.fluentjava.iwant.iwantwsrootfinder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class IwantWsRootFinderTest {

	@Test
	public void knownFileIsFoundUnderWsRoot() throws IOException {
		File wsInfo = new File(IwantWsRootFinder.essential().getParentFile(),
				"as-iwant-developer/i-have/conf/ws-info");

		assertTrue(wsInfo.exists());
		assertTrue(
				FileUtils.readFileToString(wsInfo).contains("WSNAME=iwant\n"));
	}

	@Test
	public void knownFileIsFoundUnderMockEssential() throws IOException {
		File mockedApiWsdef = new File(IwantWsRootFinder.mockEssential(),
				"iwant-api-wsdef/src/main/java/"
						+ "org/fluentjava/iwant/api/wsdef/MockedApiWsdef.java");

		assertTrue(mockedApiWsdef.exists());
		assertTrue(FileUtils.readFileToString(mockedApiWsdef)
				.contains("public class MockedApiWsdef"));
	}

}
