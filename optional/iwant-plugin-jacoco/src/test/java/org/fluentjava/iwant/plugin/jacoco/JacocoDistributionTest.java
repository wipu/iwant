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
		JacocoDistribution distro072 = JacocoDistribution
				.ofVersion("0.7.2.201409121644");
		Downloaded zip072 = distro072.zip();

		assertEquals("jacoco-0.7.2.201409121644.zip", zip072.name());
		assertEquals(
				"http://repo1.maven.org/maven2/org/jacoco/jacoco/"
						+ "0.7.2.201409121644/jacoco-0.7.2.201409121644.zip",
				zip072.url().toExternalForm());

		JacocoDistribution distroOther = JacocoDistribution
				.ofVersion("other.version");
		Downloaded zipOther = distroOther.zip();

		assertEquals("jacoco-other.version.zip", zipOther.name());
		assertEquals(
				"http://repo1.maven.org/maven2/org/jacoco/jacoco/"
						+ "other.version/jacoco-other.version.zip",
				zipOther.url().toExternalForm());
	}

	private static JacocoDistribution distroToTest() {
		return JacocoDistribution.ofVersion("0.7.2.201409121644");
	}

	private static File cachedJacocoZip() {
		return Iwant.usingRealNetwork().downloaded(distroToTest().zip().url());
	}

	public void testRealCachedZipExistsAndHasCorrectSize() {
		File cachedZip = cachedJacocoZip();

		assertTrue(cachedZip.exists());
		assertEquals(3030210, cachedZip.length());
	}

	public void testZipIsAnIngredientAndMentionedInDescriptor() {
		JacocoDistribution distro = JacocoDistribution.ofVersion("2.0.1");

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
		assertJarNameAndExistence("org.jacoco.ant-0.7.2.201409121644.jar",
				distro.orgJacocoAntJar(ctx));
		assertJarNameAndExistence("org.jacoco.core-0.7.2.201409121644.jar",
				distro.orgJacocoCoreJar(ctx));
		assertJarNameAndExistence("org.jacoco.report-0.7.2.201409121644.jar",
				distro.orgJacocoReportJar(ctx));
	}

}
