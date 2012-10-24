package net.sf.iwant.api;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.IwantNetworkMock;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.entry3.IwantEntry3TestArea;
import net.sf.iwant.entry3.TargetMock;

public class EclipseSettingsTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private SideEffectContextMock ctx;
	private Iwant iwant;
	private IwantNetworkMock network;
	private CachesMock caches;
	private File wsRoot;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.root();
		caches = new CachesMock(wsRoot);
		ctx = new SideEffectContextMock(testArea,
				new TargetEvaluationContextMock(iwant, caches));
		ctx.hasWsRoot(testArea.root());
		ctx.wsInfo().hasWsdefdefSrc(
				testArea.newDir("as-someone/i-have/wsdefdef"));
		ctx.wsInfo().hasWsName(getClass().getSimpleName());
		ctx.hasWsdefClassesTarget(new JavaClasses("wsdef-classes", Source
				.underWsroot("as-someone/i-have/wsdef"), Collections
				.<Path> emptyList()));
		caches.cachesModifiableTargetsAt(testArea.newDir("cached-modifiable"));
	}

	private void assertDotClasspathContains(String fragment) {
		assertFileContains(".classpath", fragment);
	}

	private void assertDotProjectContains(String fragment) {
		assertFileContains(".project", fragment);
	}

	private void assertFileContains(String filename, String fragment) {
		String fullActual = testArea.contentOf(filename);
		if (!fullActual.contains(fragment)) {
			assertEquals(fragment, fullActual);
		}
		if (fullActual.indexOf(fragment) != fullActual.lastIndexOf(fragment)) {
			assertEquals("File contains fragment more than once:\n" + fragment,
					fullActual);
		}
	}

	public void testDefaultValues() {
		EclipseSettings es = EclipseSettings.with().name("es").end();

		es.mutate(ctx);

		assertDotProjectContains("<name>EclipseSettingsTest</name>");

		assertDotClasspathContains("<classpathentry kind=\"src\" path=\"as-someone/i-have/wsdefdef\"/>");
		assertDotClasspathContains("<classpathentry kind=\"src\" path=\"as-someone/i-have/wsdef\"/>");
	}

	public void testDifferentValues() {
		ctx.wsInfo().hasWsdefdefSrc(
				testArea.newDir("as-someone2/i-have/wsdefdef2"));
		ctx.wsInfo().hasWsName("different-wsname");
		ctx.hasWsdefClassesTarget(new JavaClasses("wsdef-classes", Source
				.underWsroot("as-someone2/i-have/wsdef2"), Arrays.asList(
				new TargetMock("lib1"), new TargetMock("lib2"))));

		EclipseSettings es = EclipseSettings.with().name("es").end();

		es.mutate(ctx);

		assertDotProjectContains("<name>different-wsname</name>");

		assertDotClasspathContains("<classpathentry kind=\"src\" path=\"as-someone2/i-have/wsdefdef2\"/>");
		assertDotClasspathContains("<classpathentry kind=\"src\" path=\"as-someone2/i-have/wsdef2\"/>");
		assertDotClasspathContains("<classpathentry kind=\"lib\" path=\"cached-modifiable/lib1\"/>");
		assertDotClasspathContains("<classpathentry kind=\"lib\" path=\"cached-modifiable/lib2\"/>");
	}

}
