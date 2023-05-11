package org.fluentjava.iwant.plugin.findbugs;

import java.io.File;
import java.io.IOException;

import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.entry.Iwant;

public class FindbugsDistributionTest extends IwantTestCase {

	@Override
	protected void moreSetUp() {
		caches.cachesUrlAt(distroToTest().tarGz().url(), cachedFindbugsTarGz());
	}

	public void testTarGzIsADownloadedTargetWithCorrectUrlAndNameAndSum() {
		FindbugsDistribution distro300 = FindbugsDistribution._4_7_3;
		Downloaded tarGz300 = distro300.tarGz();

		assertEquals("spotbugs-4.7.3.tgz", tarGz300.name());
		assertEquals(
				"https://github.com/spotbugs/spotbugs/releases/download/"
						+ "4.7.3/spotbugs-4.7.3.tgz",
				tarGz300.url().toExternalForm());
		assertEquals("9739f70965c4b89a365419af9c688934", tarGz300.md5());

		FindbugsDistribution distro301 = FindbugsDistribution
				.ofVersion("anotherVersion", "anotherHash");
		Downloaded tarGz301 = distro301.tarGz();

		assertEquals("spotbugs-anotherVersion.tgz", tarGz301.name());
		assertEquals(
				"https://github.com/spotbugs/spotbugs/releases/download/"
						+ "anotherVersion/spotbugs-anotherVersion.tgz",
				tarGz301.url().toExternalForm());
		assertEquals("anotherHash", tarGz301.md5());
	}

	private static FindbugsDistribution distroToTest() {
		return FindbugsDistribution._4_7_3;
	}

	private static File cachedFindbugsTarGz() {
		return Iwant.usingRealNetwork()
				.downloaded(distroToTest().tarGz().url());
	}

	public void testRealCachedFindbugs301TarGzExistsAndHasCorrectSize() {
		File cachedFindbugsJar = cachedFindbugsTarGz();

		assertTrue(cachedFindbugsJar.exists());
		assertEquals(16035999, cachedFindbugsJar.length());
	}

	public void testTarGzIsAnIngredientAndMentionedInDescriptor() {
		FindbugsDistribution distro = FindbugsDistribution._4_7_3;

		assertTrue(distro.ingredients().contains(distro.tarGz()));
		assertTrue(distro.contentDescriptor().contains(distro.tarGz().name()));
	}

	public void testFindbugsHomeDirectoryUnderCachedUntarredDistro()
			throws IOException {
		FindbugsDistribution distro300 = FindbugsDistribution
				.ofVersion("anotherVersion", "whatever-hash");
		FindbugsDistribution distro301 = FindbugsDistribution._4_7_3;

		File distroHome201 = new File(ctx.cached(distro300),
				"spotbugs-anotherVersion");
		File distroHome202 = new File(ctx.cached(distro301), "spotbugs-4.7.3");

		assertEquals(distroHome201.getCanonicalPath(),
				distro300.homeDirectory(ctx).getCanonicalPath());
		assertEquals(distroHome202.getCanonicalPath(),
				distro301.homeDirectory(ctx).getCanonicalPath());
	}

	public void testFilesWithCorrectNameAreFoundInsideTheRealDistroHomeDir()
			throws Exception {
		FindbugsDistribution distro = distroToTest();

		distro.path(ctx);

		File findbugsJar = new File(distro.homeDirectory(ctx),
				"lib/spotbugs.jar");
		File findbugsAntJar = new File(distro.homeDirectory(ctx),
				"lib/spotbugs-ant.jar");

		assertTrue(findbugsJar.exists());
		assertTrue(findbugsAntJar.exists());
	}

}
