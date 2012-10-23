package net.sf.iwant.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.api.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.ExitCalledException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.entry.IwantNetworkMock;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.entry3.IwantEntry3TestArea;

public class AntGeneratedTest extends TestCase {

	private static final String ANT_VER = "1.7.1";

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

	private static URL ibiblioUrl(String group, String name, String version) {
		return Iwant.url("http://mirrors.ibiblio.org/maven2/" + group + "/"
				+ name + "/" + version + "/" + name + "-" + version + ".jar");
	}

	private static Path downloaded(URL url) throws IOException {
		Iwant iwant = Iwant.usingRealNetwork();
		iwant.downloaded(url);
		return new ExternalSource(iwant.network().cacheLocation(
				new UnmodifiableUrl(url)));
	}

	private static Path antJar() throws IOException {
		return downloaded(ibiblioUrl("org/apache/ant", "ant", ANT_VER));
	}

	private static Path antLauncherJar() throws IOException {
		return downloaded(ibiblioUrl("org/apache/ant", "ant-launcher", ANT_VER));
	}

	public void testContentDescriptor() throws IOException {
		assertEquals(
				"net.sf.iwant.api.AntGenerated {\n"
						+ "  ant-jar:/home/wipu/.net.sf.iwant/cached/UnmodifiableUrl/http%3A/%2Fmirrors.ibiblio.org/maven2/org/apache/ant/ant/1.7.1/ant-1.7.1.jar\n"
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
		assertTrue(err().contains("hello message"));
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
		assertTrue(err().contains("fail message"));
	}

}
