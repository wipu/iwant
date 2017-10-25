package net.sf.iwant.plugin.findbugs;

import java.io.File;
import java.io.IOException;

import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.core.download.Downloaded;
import net.sf.iwant.entry.Iwant;

public class FindbugsDistributionTest extends IwantTestCase {

	@Override
	protected void moreSetUp() {
		caches.cachesUrlAt(distroToTest().tarGz().url(), cachedFindbugsTarGz());
	}

	public void testTarGzIsADownloadedTargetWithCorrectUrlAndName() {
		FindbugsDistribution distro139 = FindbugsDistribution
				.ofVersion("1.3.9");
		Downloaded tarGz139 = distro139.tarGz();

		assertEquals("findbugs-1.3.9.tar.gz", tarGz139.name());
		assertEquals(
				"http://downloads.sourceforge.net/project/findbugs/findbugs/"
						+ "1.3.9/findbugs-1.3.9.tar.gz",
				tarGz139.url().toExternalForm());

		FindbugsDistribution distro202 = FindbugsDistribution
				.ofVersion("2.0.2");
		Downloaded tarGz202 = distro202.tarGz();

		assertEquals("findbugs-2.0.2.tar.gz", tarGz202.name());
		assertEquals(
				"http://downloads.sourceforge.net/project/findbugs/findbugs/"
						+ "2.0.2/findbugs-2.0.2.tar.gz",
				tarGz202.url().toExternalForm());
	}

	private static FindbugsDistribution distroToTest() {
		return FindbugsDistribution.ofVersion("3.0.0");
	}

	private static File cachedFindbugsTarGz() {
		return Iwant.usingRealNetwork()
				.downloaded(distroToTest().tarGz().url());
	}

	public void testRealCachedFindbugs202TarGzExistsAndHasCorrectSize() {
		File cachedFindbugsJar = cachedFindbugsTarGz();

		assertTrue(cachedFindbugsJar.exists());
		assertEquals(8893609, cachedFindbugsJar.length());
	}

	public void testTarGzIsAnIngredientAndMentionedInDescriptor() {
		FindbugsDistribution distro = FindbugsDistribution.ofVersion("2.0.1");

		assertTrue(distro.ingredients().contains(distro.tarGz()));
		assertTrue(distro.contentDescriptor().contains(distro.tarGz().name()));
	}

	public void testFindbugsHomeDirectoryUnderCachedUntarredDistro()
			throws IOException {
		FindbugsDistribution distro201 = FindbugsDistribution
				.ofVersion("2.0.1");
		FindbugsDistribution distro202 = FindbugsDistribution
				.ofVersion("2.0.2");

		File distroHome201 = new File(ctx.cached(distro201), "findbugs-2.0.1");
		File distroHome202 = new File(ctx.cached(distro202), "findbugs-2.0.2");

		assertEquals(distroHome201.getCanonicalPath(),
				distro201.homeDirectory(ctx).getCanonicalPath());
		assertEquals(distroHome202.getCanonicalPath(),
				distro202.homeDirectory(ctx).getCanonicalPath());
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
