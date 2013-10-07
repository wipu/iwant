package net.sf.iwant.plugin.ant;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testing.IwantNetworkMock;

public class JarTest extends TestCase {

	private IwantPluginAntTestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private File cached;
	private CachesMock caches;

	@Override
	public void setUp() {
		testArea = new IwantPluginAntTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = new File(testArea.root(), "wsRoot");
		caches = new CachesMock(wsRoot);
		ctx = new TargetEvaluationContextMock(iwant, caches);
		ctx.hasWsRoot(wsRoot);
		cached = testArea.newDir("cached");
		caches.cachesModifiableTargetsAt(cached);
	}

	private void assertJarContainsTestStuff(File cachedJar) {
		File tmp = testArea.newDir("tmp");
		Unzipped.unzipTo(cachedJar, tmp);

		assertTrue(new File(tmp, "a.txt").exists());
		assertEquals("Mock A.class\n",
				testArea.contentOf(new File(tmp, "a.txt")));
		assertTrue(new File(tmp, "META-INF/MANIFEST.MF").exists());
	}

	public void testIngredientsAndDescriptorOfSimpleJarOfClasses() {
		Jar jar = Jar.with().classes(Source.underWsroot("classes")).end();
		assertEquals("[classes]", jar.ingredients().toString());
		assertEquals("net.sf.iwant.plugin.ant.Jar: {\n  ingredients: {\n"
				+ "    classes\n  }\n}\n", jar.contentDescriptor());
	}

	public void testIngredientsAndDescriptorOfJarOfClassesUnderSubDirectory() {
		Jar jar = Jar.with().classes(Source.underWsroot("classes"))
				.classesSubDirectory("classes-subdir").end();
		assertEquals("[classes]", jar.ingredients().toString());
		assertEquals("net.sf.iwant.plugin.ant.Jar: {\n"
				+ "  classes-sub-directory:classes-subdir\n"
				+ "  ingredients: {\n    classes\n  }\n}\n",
				jar.contentDescriptor());
	}

	public void testJarOfDirectory() throws Exception {
		File classes = new File(getClass().getResource(
				"/net/sf/iwant/plugin/ant/dirtojar").toURI());

		Target jar = Jar.with().name("test.jar")
				.classes(new ExternalSource(classes)).end();
		jar.path(ctx);

		File cachedJar = new File(cached, "test.jar");
		assertTrue(cachedJar.exists());
		assertJarContainsTestStuff(cachedJar);
	}

	public void testJarOfDirectoryThatContainsClassesInSubDirectory()
			throws Exception {
		File classes = new File(getClass().getResource(
				"/net/sf/iwant/plugin/ant").toURI());

		Target jar = Jar.with().name("test.jar")
				.classesSubDirectory("dirtojar")
				.classes(new ExternalSource(classes)).end();
		jar.path(ctx);

		File cachedJar = new File(cached, "test.jar");
		assertTrue(cachedJar.exists());
		assertJarContainsTestStuff(cachedJar);
	}

}
