package net.sf.iwant.api;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import net.sf.iwant.api.model.Concatenated;
import net.sf.iwant.api.model.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.ExitCalledException;

public class AntGeneratedTest extends IwantTestCase {

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
				contentOf(new File(cacheDir, "ant")));
	}

}
