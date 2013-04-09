package net.sf.iwant.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;
import net.sf.iwant.api.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.ExitCalledException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.testing.IwantEntry3TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class AntGeneratedTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private Iwant iwant;
	private IwantNetwork network;
	private CachesMock caches;
	private File wsRoot;
	private TargetEvaluationContextMock ctx;
	private File cacheDir;

	private PrintStream oldOut;

	private PrintStream oldErr;

	private ByteArrayOutputStream out;

	private ByteArrayOutputStream err;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.root();
		caches = new CachesMock(wsRoot);
		cacheDir = testArea.newDir("cache");
		caches.cachesModifiableTargetsAt(cacheDir);
		ctx = new TargetEvaluationContextMock(iwant, caches);

		oldOut = System.out;
		oldErr = System.err;
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}

	@Override
	protected void tearDown() throws Exception {
		System.setErr(oldErr);
		System.setOut(oldOut);
	}

	private String out() {
		return out.toString();
	}

	private String err() {
		return err.toString();
	}

	private static void assertContains(String full, String fragment) {
		if (!full.contains(fragment)) {
			assertEquals("Should contain:\n" + fragment, full);
		}
	}

	private Path downloaded(Path downloaded) throws IOException {
		return new ExternalSource(AsEmbeddedIwantUser.with()
				.workspaceAt(wsRoot).cacheAt(cacheDir).iwant()
				.target((Target) downloaded).asPath());
	}

	private Path antJar() throws IOException {
		return downloaded(TestedIwantDependencies.antJar());
	}

	private Path antLauncherJar() throws IOException {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	public void testContentDescriptor() throws IOException {
		assertEquals(
				"net.sf.iwant.api.AntGenerated {\n"
						+ "  ant-jar:"
						+ Iwant.IWANT_USER_DIR
						+ "/cached/UnmodifiableUrl/http%3A/%2Fmirrors.ibiblio.org/maven2/org/apache/ant/ant/1.7.1/ant-1.7.1.jar\n"
						+ "  script:script\n" + "}\n",
				AntGenerated.with().name("minimal").antJars(antJar())
						.script(Source.underWsroot("script")).end()
						.contentDescriptor());
		assertEquals(
				"net.sf.iwant.api.AntGenerated {\n" + "  ant-jar:" + antJar()
						+ "\n" + "  ant-jar:another-ant.jar\n"
						+ "  script:another-script\n" + "}\n" + "",
				AntGenerated
						.with()
						.name("another")
						.antJars(antJar(),
								Source.underWsroot("another-ant.jar"))
						.script(Source.underWsroot("another-script")).end()
						.contentDescriptor());
	}

	public void testIngredients() throws IOException {
		assertEquals(
				"[" + antJar() + ", script]",
				AntGenerated.with().name("minimal").antJars(antJar())
						.script(Source.underWsroot("script")).end()
						.ingredients().toString());
		assertEquals(
				"[" + antJar() + ", another-ant.jar, another-script]",
				AntGenerated
						.with()
						.name("another")
						.antJars(antJar(),
								Source.underWsroot("another-ant.jar"))
						.script(Source.underWsroot("another-script")).end()
						.ingredients().toString());
	}

	public void testMinimalEcho() throws Exception {
		ConcatenatedBuilder scriptContent = Concatenated.named("script");
		scriptContent.string("<project name='hello' default='hello'>\n");
		scriptContent.string("  <target name='hello'>\n");
		scriptContent.string("    <echo message='hello message'/>\n");
		scriptContent.string("  </target>\n");
		scriptContent.string("</project>\n");
		Concatenated script = scriptContent.end();
		script.path(ctx);

		AntGenerated antGen = AntGenerated.with().name("ant")
				.antJars(antJar(), antLauncherJar()).script(script).end();
		antGen.path(ctx);

		assertEquals("", out());
		assertContains(err(), "hello message");
	}

	public void testMinimalFail() throws Exception {
		ConcatenatedBuilder scriptContent = Concatenated.named("script");
		scriptContent.string("<project name='fail' default='fail'>\n");
		scriptContent.string("  <target name='fail'>\n");
		scriptContent.string("    <fail message='fail message'/>\n");
		scriptContent.string("  </target>\n");
		scriptContent.string("</project>\n");
		Concatenated script = scriptContent.end();
		script.path(ctx);

		AntGenerated antGen = AntGenerated.with().name("ant")
				.antJars(antJar(), antLauncherJar()).script(script).end();
		try {
			antGen.path(ctx);
			fail();
		} catch (InvocationTargetException e) {
			ExitCalledException ece = (ExitCalledException) e.getCause();
			assertEquals(1, ece.status());
		}

		assertEquals("", out());
		assertContains(err(), "fail message");
	}

	public void testEchoIwantOutFileProperty() throws Exception {
		ConcatenatedBuilder scriptContent = Concatenated.named("script");
		scriptContent.string("<project name='hello' default='hello'>\n");
		scriptContent.string("  <target name='hello'>\n");
		scriptContent.string("    <echo message='((${iwant-outfile}))'/>\n");
		scriptContent.string("  </target>\n");
		scriptContent.string("</project>\n");
		Concatenated script = scriptContent.end();
		script.path(ctx);

		AntGenerated antGen = AntGenerated.with().name("ant")
				.antJars(antJar(), antLauncherJar()).script(script).end();
		antGen.path(ctx);

		assertEquals("", out());
		assertContains(err(), "((" + cacheDir + "/ant))");
	}

	public void testFileGeneratingScriptWithIngredients() throws Exception {
		HelloTarget ingredient1 = new HelloTarget("ingredient1",
				"ingredient1 content");
		ingredient1.path(ctx);
		HelloTarget ingredient2 = new HelloTarget("ingredient2",
				"ingredient2 content");
		ingredient2.path(ctx);

		ConcatenatedBuilder scriptContent = Concatenated.named("script");
		scriptContent.string("<project name='hello' default='hello'>\n");
		scriptContent.string("  <target name='hello'>\n");
		scriptContent.string("    <copy file='").pathTo(ingredient1)
				.string("' tofile='${iwant-outfile}'/>\n");
		scriptContent
				.string("    <echo file='${iwant-outfile}' append='true'"
						+ " message=' appended with ").contentOf(ingredient2)
				.string("'/>\n");
		scriptContent.string("  </target>\n");
		scriptContent.string("</project>\n");
		Concatenated script = scriptContent.end();
		script.path(ctx);

		AntGenerated antGen = AntGenerated.with().name("ant")
				.antJars(antJar(), antLauncherJar()).script(script).end();
		antGen.path(ctx);

		assertEquals("", out());
		assertContains(err(), "[copy]");

		assertEquals("ingredient1 content appended with ingredient2 content",
				testArea.contentOf(new File(cacheDir, "ant")));
	}

}
