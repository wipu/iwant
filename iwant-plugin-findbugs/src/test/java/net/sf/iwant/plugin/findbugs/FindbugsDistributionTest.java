package net.sf.iwant.plugin.findbugs;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import net.sf.iwant.api.Downloaded;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testing.IwantNetworkMock;

public class FindbugsDistributionTest extends TestCase {

	private IwantPluginFindbugsTestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private File cached;
	private CachesMock caches;

	@Override
	public void setUp() {
		testArea = new IwantPluginFindbugsTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = new File(testArea.root(), "wsRoot");
		caches = new CachesMock(wsRoot);
		ctx = new TargetEvaluationContextMock(iwant, caches);
		ctx.hasWsRoot(wsRoot);
		cached = new File(testArea.root(), "cached");
		caches.cachesModifiableTargetsAt(cached);
		caches.cachesUrlAt(distroToTest().tarGz().url(), cachedFindbugsTarGz());
	}

	public void testTarGzIsADownloadedTargetWithCorrectUrlAndName() {
		FindbugsDistribution distro139 = FindbugsDistribution
				.ofVersion("1.3.9");
		Downloaded tarGz139 = distro139.tarGz();

		assertEquals("findbugs-1.3.9.tar.gz", tarGz139.name());
		assertEquals(
				"http://downloads.sourceforge.net/project/findbugs/findbugs/"
						+ "1.3.9/findbugs-1.3.9.tar.gz", tarGz139.url()
						.toExternalForm());

		FindbugsDistribution distro202 = FindbugsDistribution
				.ofVersion("2.0.2");
		Downloaded tarGz202 = distro202.tarGz();

		assertEquals("findbugs-2.0.2.tar.gz", tarGz202.name());
		assertEquals(
				"http://downloads.sourceforge.net/project/findbugs/findbugs/"
						+ "2.0.2/findbugs-2.0.2.tar.gz", tarGz202.url()
						.toExternalForm());
	}

	private static FindbugsDistribution distroToTest() {
		return FindbugsDistribution.ofVersion("2.0.2");
	}

	private static File cachedFindbugsTarGz() {
		return Iwant.usingRealNetwork()
				.downloaded(distroToTest().tarGz().url());
	}

	public void testRealCachedFindbugs202TarGzExistsAndHasCorrectSize() {
		File cachedFindbugsJar = cachedFindbugsTarGz();

		assertTrue(cachedFindbugsJar.exists());
		assertEquals(8295637, cachedFindbugsJar.length());
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
