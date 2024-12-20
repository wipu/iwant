package org.fluentjava.iwant.core.ant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.embedded.AsEmbeddedIwantUser;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.ExitCalledException;
import org.junit.jupiter.api.Test;

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
		return ExternalSource.at(AsEmbeddedIwantUser.with().workspaceAt(wsRoot)
				.cacheAt(cached).iwant().target((Target) downloaded).asPath());
	}

	private Path antJar() {
		return downloaded(TestedIwantDependencies.antJar());
	}

	private Path antLauncherJar() {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	@Test
	public void contentDescriptor() {
		assertEquals("org.fluentjava.iwant.core.ant.AntGenerated\n"
				+ "i:ant-jars:\n  " + Iwant.IWANT_USER_DIR
				+ "/cached/UnmodifiableUrl/https%3A/%2Frepo1.maven.org/maven2/org/apache/ant/ant/1.10.14/ant-1.10.14.jar\n"
				+ "i:script:\n  script\n",
				AntGenerated.with().name("minimal").antJars(antJar())
						.script(Source.underWsroot("script")).end()
						.contentDescriptor());
		assertEquals("org.fluentjava.iwant.core.ant.AntGenerated\n"
				+ "i:ant-jars:\n  " + antJar() + "\n" + "  another-ant.jar\n"
				+ "i:script:\n  another-script\n",
				AntGenerated.with().name("another")
						.antJars(antJar(),
								Source.underWsroot("another-ant.jar"))
						.script(Source.underWsroot("another-script")).end()
						.contentDescriptor());
	}

	@Test
	public void ingredients() {
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

	@Test
	public void minimalEcho() throws Exception {
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

	@Test
	public void minimalFail() throws Exception {
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

	@Test
	public void echoIwantOutFileProperty() throws Exception {
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

	@Test
	public void fileGeneratingScriptWithIngredients() throws Exception {
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
