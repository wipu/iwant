package net.sf.iwant.eclipsesettings;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.api.SideEffectContextMock;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Concatenated;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class EclipseSettingsWriterTest extends TestCase {

	private TestArea testArea;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private CachesMock caches;
	private TargetEvaluationContextMock evCtx;
	private SideEffectContextMock seCtx;

	@Override
	protected void setUp() throws Exception {
		testArea = new EclipseSettingsTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.root();
		caches = new CachesMock(wsRoot);
		evCtx = new TargetEvaluationContextMock(iwant, caches);
		seCtx = new SideEffectContextMock(testArea, evCtx);
		seCtx.hasWsRoot(wsRoot);
		seCtx.wsInfo().hasRelativeAsSomeone("as-writer-test");
	}

	public void testMinimal() {
		JavaModule module = JavaSrcModule.with().name("minimal").end();

		EclipseSettingsWriter.with().modules(module).context(seCtx).end()
				.write();

		assertTrue(testArea.contentOf("minimal/.project").contains(
				"<name>minimal</name>"));
		assertTrue(testArea.contentOf("minimal/.classpath").contains(
				"<classpathentry"));
		assertTrue(testArea.contentOf(
				"minimal/.settings/org.eclipse.jdt.core.prefs").contains(
				"org.eclipse.jdt.core.compiler.problem.deadCode=warning\n"));
		assertTrue(testArea.contentOf(
				"minimal/.settings/org.eclipse.jdt.ui.prefs").contains(
				"formatter_settings_version=12\n"));
	}

	public void testMinimalWithLocationThatDiffersFromName() {
		JavaModule module = JavaSrcModule.with().name("module-name")
				.locationUnderWsRoot("parent/dir-name").end();

		EclipseSettingsWriter.with().modules(module).context(seCtx).end()
				.write();

		assertTrue(testArea.contentOf("parent/dir-name/.project").contains(
				"<name>module-name</name>"));
		assertTrue(testArea.contentOf("parent/dir-name/.classpath").contains(
				"<classpathentry"));
		assertTrue(testArea
				.contentOf(
						"parent/dir-name/.settings/org.eclipse.jdt.core.prefs")
				.contains(
						"org.eclipse.jdt.core.compiler.problem.deadCode=warning\n"));
		assertTrue(testArea.contentOf(
				"parent/dir-name/.settings/org.eclipse.jdt.ui.prefs").contains(
				"formatter_settings_version=12\n"));
	}

	public void testCodeGeneration() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.pathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.exportsClasses(generatedClasses, generatedSrc).end();

		EclipseSettingsWriter.with().modules(module).context(seCtx).end()
				.write();

		assertTrue(testArea.contentOf("code-generating-module/.project")
				.contains("<name>code-generating-module</name>"));
		assertTrue(testArea.contentOf("code-generating-module/.classpath")
				.contains("<classpathentry exported=\"true\""));

		assertTrue(testArea.contentOf(
				"code-generating-module/eclipse-ant-build.xml").contains(
				"name=\"code-generating-module-eclipse-ant-build\""));
		assertTrue(testArea
				.contentOf("code-generating-module/eclipse-ant-build.xml")
				.contains(
						"<property name=\"as-someone\" location=\"${basedir}/as-writer-test\" />"));

		assertTrue(testArea
				.contentOf(
						"code-generating-module/.externalToolBuilders/code-generating-module.launch")
				.contains("code-generating-module"));
	}

}
