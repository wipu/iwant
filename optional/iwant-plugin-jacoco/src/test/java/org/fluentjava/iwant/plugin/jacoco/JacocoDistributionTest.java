package org.fluentjava.iwant.plugin.jacoco;

import java.io.File;

import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.entry.Iwant;

public class JacocoDistributionTest extends IwantTestCase {

	@Override
	protected void moreSetUp() {
		caches.cachesUrlAt(distroToTest().zip().url(), cachedJacocoZip());
	}

	public void testZipIsADownloadedTargetWithCorrectUrlAndName() {
		JacocoDistribution distroNewest = distroToTest();
		Downloaded zipNewest = distroNewest.zip();

		assertEquals("jacoco-0.8.4.zip", zipNewest.name());
		assertEquals(
				"https://github.com/jacoco/jacoco/releases/download/"
						+ "v0.8.4/jacoco-0.8.4.zip",
				zipNewest.url().toExternalForm());

		JacocoDistribution distroOlder = JacocoDistribution.ofVersion("0.8.3");
		Downloaded zipOlder = distroOlder.zip();

		assertEquals("jacoco-0.8.3.zip", zipOlder.name());
		assertEquals(
				"https://github.com/jacoco/jacoco/releases/download/"
						+ "v0.8.3/jacoco-0.8.3.zip",
				zipOlder.url().toExternalForm());
	}

	private static JacocoDistribution distroToTest() {
		return JacocoDistribution.newestTestedVersion();
	}

	private static File cachedJacocoZip() {
		return Iwant.usingRealNetwork().downloaded(distroToTest().zip().url());
	}

	public void testRealCachedZipExistsAndHasCorrectSize() {
		File cachedZip = cachedJacocoZip();

		assertTrue(cachedZip.exists());
		assertEquals(3860930, cachedZip.length());
	}

	public void testZipIsAnIngredientAndMentionedInDescriptor() {
		JacocoDistribution distro = JacocoDistribution.ofVersion("something");

		assertTrue(distro.ingredients().contains(distro.zip()));
		assertTrue(distro.contentDescriptor().contains(distro.zip().name()));
	}

	private static void assertJarNameAndExistence(String name, File jar) {
		assertEquals(name, jar.getName());
		assertTrue(jar.exists());
	}

	public void testJarFilesWithCorrectNameAreFoundInsideTheRealDistro()
			throws Exception {
		JacocoDistribution distro = distroToTest();

		distro.path(ctx);

		assertJarNameAndExistence("jacocoagent.jar",
				distro.jacocoagentJar(ctx));
		assertJarNameAndExistence("jacocoant.jar", distro.jacocoantJar(ctx));
	}

}
