package net.sf.iwant.api;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.entry3.TargetMock;
import net.sf.iwant.testing.IwantEntry3TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class EclipseSettingsTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private SideEffectContextMock ctx;
	private Iwant iwant;
	private IwantNetworkMock network;
	private CachesMock caches;
	private File wsRoot;
	private File cacheDir;

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
		File wsdefdef = testArea.newDir("as-someone/i-have/wsdefdef");
		testArea.newDir("as-someone/i-have/wsdef");
		ctx.wsInfo().hasWsdefdefModule(wsdefdef);
		ctx.wsInfo().hasWsName(getClass().getSimpleName());
		cacheDir = testArea.newDir("cached-modifiable");
		caches.cachesModifiableTargetsAt(cacheDir);
	}

	private void assertDotClasspathContains(String project, String fragment) {
		assertFileContains(project + "/.classpath", fragment);
	}

	private void assertDotProjectContains(String project, String fragment) {
		assertFileContains(project + "/.project", fragment);
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

	public void testMutationUsingWsdefdefAndWsdefAndAnotherModuleUsedByWsdef() {
		JavaModule iwantClasses = JavaModule.implicitLibrary(TargetMock
				.ingredientless("iwant-classes"));
		JavaModule wsdefdef = JavaModule.with().name("test-wsdefdef")
				.locationUnderWsRoot("as-someone/i-have/wsdefdef")
				.mainJava("src/main/java").mainDeps(iwantClasses).end();
		testArea.newDir("utils/wsdef-tools");
		JavaModule wsdefTools = JavaModule.with().name("test-wsdef-tools")
				.locationUnderWsRoot("utils/wsdef-tools").mainJava("src").end();
		JavaModule wsdef = JavaModule.with().name("test-wsdef")
				.locationUnderWsRoot("as-someone/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(iwantClasses, wsdefTools)
				.end();
		EclipseSettings es = EclipseSettings.with()
				.modules(wsdefdef, wsdefTools, wsdef).name("es").end();

		es.mutate(ctx);

		assertDotProjectContains("as-someone/i-have/wsdefdef",
				"<name>test-wsdefdef</name>");
		assertDotProjectContains("as-someone/i-have/wsdef",
				"<name>test-wsdef</name>");
		assertDotProjectContains("utils/wsdef-tools",
				"<name>test-wsdef-tools</name>");

		assertDotClasspathContains("as-someone/i-have/wsdefdef",
				"<classpathentry kind=\"src\" path=\"src/main/java\"/>");
		assertDotClasspathContains("as-someone/i-have/wsdefdef",
				"<classpathentry kind=\"lib\" path=\"" + cacheDir
						+ "/iwant-classes\"/>");

		assertDotClasspathContains("utils/wsdef-tools",
				"<classpathentry kind=\"src\" path=\"src\"/>");

		assertDotClasspathContains("as-someone/i-have/wsdef",
				"<classpathentry kind=\"src\" path=\"src/main/java\"/>");
		assertDotClasspathContains("as-someone/i-have/wsdef",
				"<classpathentry kind=\"lib\" path=\"" + cacheDir
						+ "/iwant-classes\"/>");
		assertDotClasspathContains("as-someone/i-have/wsdef",
				"<classpathentry combineaccessrules=\"false\""
						+ " kind=\"src\" path=\"/test-wsdef-tools\"/>");
	}

	public void testNoSourcesMeansNoSources() {
		JavaModule srcless = JavaModule.with().name("any")
				.locationUnderWsRoot("any").end();
		testArea.newDir("any");

		EclipseSettings es = EclipseSettings.with().modules(srcless).name("es")
				.end();

		es.mutate(ctx);

		assertFalse(testArea.contentOf("any/.classpath").contains(
				"<classpathentry kind=\"src\" "));
	}

	public void testTestJavaAndTestDepsAffectDotClasspath() {
		JavaModule testTools1 = JavaModule.implicitLibrary(TargetMock
				.ingredientless("test-tools-1"));
		JavaModule testTools2 = JavaModule.implicitLibrary(TargetMock
				.ingredientless("test-tools-2"));

		JavaModule mod1 = JavaModule.with().name("mod1")
				.locationUnderWsRoot("mod1").mainJava("src").testJava("tests1")
				.testDeps(testTools1).end();
		JavaModule mod2 = JavaModule.with().name("mod2")
				.locationUnderWsRoot("mod2").mainJava("src2")
				.testJava("tests2").testDeps(testTools2).end();
		testArea.newDir("mod1");
		testArea.newDir("mod2");

		EclipseSettings es = EclipseSettings.with().modules(mod1, mod2)
				.name("es").end();

		es.mutate(ctx);

		assertDotClasspathContains("mod1",
				"<classpathentry kind=\"src\" path=\"tests1\"/>");
		assertDotClasspathContains("mod2",
				"<classpathentry kind=\"src\" path=\"tests2\"/>");

		assertDotClasspathContains("mod1",
				"<classpathentry kind=\"lib\" path=\"" + cacheDir
						+ "/test-tools-1\"/>");
		assertDotClasspathContains("mod2",
				"<classpathentry kind=\"lib\" path=\"" + cacheDir
						+ "/test-tools-2\"/>");

	}

}
