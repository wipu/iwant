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
		FindbugsDistribution distro300 = FindbugsDistribution._3_0_0;
		Downloaded tarGz300 = distro300.tarGz();

		assertEquals("findbugs-3.0.0.tar.gz", tarGz300.name());
		assertEquals(
				"http://downloads.sourceforge.net/project/findbugs/findbugs/"
						+ "3.0.0/findbugs-3.0.0.tar.gz",
				tarGz300.url().toExternalForm());
		assertEquals("f0915a0800a926961296da28b6ada7cc", tarGz300.md5());

		FindbugsDistribution distro301 = FindbugsDistribution._3_0_1;
		Downloaded tarGz301 = distro301.tarGz();

		assertEquals("findbugs-3.0.1.tar.gz", tarGz301.name());
		assertEquals(
				"http://downloads.sourceforge.net/project/findbugs/findbugs/"
						+ "3.0.1/findbugs-3.0.1.tar.gz",
				tarGz301.url().toExternalForm());
		assertEquals("dec8828de8657910fcb258ce5383c168", tarGz301.md5());
	}

	private static FindbugsDistribution distroToTest() {
		return FindbugsDistribution._3_0_1;
	}

	private static File cachedFindbugsTarGz() {
		return Iwant.usingRealNetwork()
				.downloaded(distroToTest().tarGz().url());
	}

	public void testRealCachedFindbugs301TarGzExistsAndHasCorrectSize() {
		File cachedFindbugsJar = cachedFindbugsTarGz();

		assertTrue(cachedFindbugsJar.exists());
		assertEquals(9120840, cachedFindbugsJar.length());
	}

	public void testTarGzIsAnIngredientAndMentionedInDescriptor() {
		FindbugsDistribution distro = FindbugsDistribution._3_0_1;

		assertTrue(distro.ingredients().contains(distro.tarGz()));
		assertTrue(distro.contentDescriptor().contains(distro.tarGz().name()));
	}

	public void testFindbugsHomeDirectoryUnderCachedUntarredDistro()
			throws IOException {
		FindbugsDistribution distro300 = FindbugsDistribution._3_0_0;
		FindbugsDistribution distro301 = FindbugsDistribution._3_0_1;

		File distroHome201 = new File(ctx.cached(distro300), "findbugs-3.0.0");
		File distroHome202 = new File(ctx.cached(distro301), "findbugs-3.0.1");

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
				"lib/findbugs.jar");
		File findbugsAntJar = new File(distro.homeDirectory(ctx),
				"lib/findbugs-ant.jar");

		assertTrue(findbugsJar.exists());
		assertTrue(findbugsAntJar.exists());
	}

}
