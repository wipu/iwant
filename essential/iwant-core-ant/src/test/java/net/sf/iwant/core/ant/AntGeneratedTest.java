package net.sf.iwant.core.ant;

import java.io.File;

import net.sf.iwant.api.core.Concatenated;
import net.sf.iwant.api.core.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.embedded.AsEmbeddedIwantUser;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.ExitCalledException;

public class AntGeneratedTest extends IwantTestCase {

	/**
	 * See @antstderrbug in backlog
	 */
	private static final boolean TEST_ANT_STDERR_BUG = false;

	private static void assertContains(String full, String fragment) {
		if (!full.contains(fragment)) {
			assertEquals("Should contain:\n" + fragment, full);
		}
	}

	private Path downloaded(Path downloaded) {
		return new ExternalSource(AsEmbeddedIwantUser.with().workspaceAt(wsRoot)
				.cacheAt(cached).iwant().target((Target) downloaded).asPath());
	}

	private Path antJar() {
		return downloaded(TestedIwantDependencies.antJar());
	}

	private Path antLauncherJar() {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	public void testContentDescriptor() {
		assertEquals(
				"net.sf.iwant.core.ant.AntGenerated\n" + "i:ant-jars:\n  "
						+ Iwant.IWANT_USER_DIR
						+ "/cached/UnmodifiableUrl/http%3A/%2Frepo1.maven.org/maven2/org/apache/ant/ant/1.10.1/ant-1.10.1.jar\n"
						+ "i:script:\n  script\n",
				AntGenerated.with().name("minimal").antJars(antJar())
						.script(Source.underWsroot("script")).end()
						.contentDescriptor());
		assertEquals(
				"net.sf.iwant.core.ant.AntGenerated\n" + "i:ant-jars:\n  "
						+ antJar() + "\n" + "  another-ant.jar\n"
						+ "i:script:\n  another-script\n",
				AntGenerated.with().name("another")
						.antJars(antJar(),
								Source.underWsroot("another-ant.jar"))
						.script(Source.underWsroot("another-script")).end()
						.contentDescriptor());
	}

	public void testIngredients() {
		assertEquals("[" + antJar() + ", script]",
				AntGenerated.with().name("minimal").antJars(antJar())
						.script(Source.underWsroot("script")).end()
						.ingredients().toString());
		assertEquals("[" + antJar() + ", another-ant.jar, another-script]",
				AntGenerated.with().name("another")
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
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
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
		if (TEST_ANT_STDERR_BUG) {
			assertContains(err(), "((" + cached + "/ant))");
		}
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
		scriptContent.string("    <copy file='").nativePathTo(ingredient1)
				.string("' tofile='${iwant-outfile}'/>\n");
		scriptContent
				.string("    <echo file='${iwant-outfile}' append='true'"
						+ " message=' appended with ")
				.contentOf(ingredient2).string("'/>\n");
		scriptContent.string("  </target>\n");
		scriptContent.string("</project>\n");
		Concatenated script = scriptContent.end();
		script.path(ctx);

		AntGenerated antGen = AntGenerated.with().name("ant")
				.antJars(antJar(), antLauncherJar()).script(script).end();
		antGen.path(ctx);

		assertEquals("", out());
		if (TEST_ANT_STDERR_BUG) {
			assertContains(err(), "[copy]");
		}

		assertEquals("ingredient1 content appended with ingredient2 content",
				contentOf(new File(cached, "ant")));
	}

}
