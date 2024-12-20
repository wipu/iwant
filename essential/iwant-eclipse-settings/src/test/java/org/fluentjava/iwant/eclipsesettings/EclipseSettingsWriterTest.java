package org.fluentjava.iwant.eclipsesettings;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.junit.jupiter.api.Test;

public class EclipseSettingsWriterTest extends IwantTestCase {

	@Override
	protected void moreSetUp() {
		seCtx.wsInfo().hasRelativeAsSomeone("as-writer-test");
	}

	@Test
	public void minimal() {
		JavaModule module = JavaSrcModule.with().name("minimal").end();

		EclipseSettingsWriter.with().modules(module).context(seCtx).end()
				.write();

		assertTrue(contentOfFileUnderWsRoot("minimal/.project")
				.contains("<name>minimal</name>"));
		assertTrue(contentOfFileUnderWsRoot("minimal/.classpath")
				.contains("<classpathentry"));
		assertTrue(contentOfFileUnderWsRoot(
				"minimal/.settings/org.eclipse.jdt.core.prefs").contains(
						"org.eclipse.jdt.core.compiler.problem.deadCode=warning\n"));
		assertTrue(contentOfFileUnderWsRoot(
				"minimal/.settings/org.eclipse.jdt.ui.prefs")
						.contains("formatter_settings_version=12\n"));
		assertTrue(contentOfFileUnderWsRoot(
				"minimal/.settings/org.eclipse.core.resources.prefs")
						.contains("=UTF-8"));
	}

	@Test
	public void minimalWithLocationThatDiffersFromName() {
		JavaModule module = JavaSrcModule.with().name("module-name")
				.locationUnderWsRoot("parent/dir-name").end();

		EclipseSettingsWriter.with().modules(module).context(seCtx).end()
				.write();

		assertTrue(contentOfFileUnderWsRoot("parent/dir-name/.project")
				.contains("<name>module-name</name>"));
		assertTrue(contentOfFileUnderWsRoot("parent/dir-name/.classpath")
				.contains("<classpathentry"));
		assertTrue(contentOfFileUnderWsRoot(
				"parent/dir-name/.settings/org.eclipse.jdt.core.prefs")
						.contains(
								"org.eclipse.jdt.core.compiler.problem.deadCode=warning\n"));
		assertTrue(contentOfFileUnderWsRoot(
				"parent/dir-name/.settings/org.eclipse.jdt.ui.prefs")
						.contains("formatter_settings_version=12\n"));
		assertTrue(contentOfFileUnderWsRoot(
				"parent/dir-name/.settings/org.eclipse.core.resources.prefs")
						.contains("=UTF-8"));
	}

	@Test
	public void codeGeneration() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.nativePathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.exportsClasses(generatedClasses, generatedSrc).end();

		EclipseSettingsWriter.with().modules(module).context(seCtx).end()
				.write();

		assertTrue(contentOfFileUnderWsRoot("code-generating-module/.project")
				.contains("<name>code-generating-module</name>"));
		assertTrue(contentOfFileUnderWsRoot("code-generating-module/.classpath")
				.contains("<classpathentry exported=\"true\""));

		assertTrue(contentOfFileUnderWsRoot(
				"code-generating-module/eclipse-ant-build.xml").contains(
						"name=\"code-generating-module-eclipse-ant-build\""));
		assertTrue(contentOfFileUnderWsRoot(
				"code-generating-module/eclipse-ant-build.xml").contains(
						"<property name=\"as-someone\" location=\"${basedir}/as-writer-test\" />"));

		assertTrue(contentOfFileUnderWsRoot(
				"code-generating-module/.externalToolBuilders/code-generating-module.launch")
						.contains("code-generating-module"));
	}

}
