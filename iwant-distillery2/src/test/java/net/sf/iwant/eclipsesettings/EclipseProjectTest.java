package net.sf.iwant.eclipsesettings;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.api.javamodules.CodeFormatterPolicy;
import net.sf.iwant.api.javamodules.CodeStyle;
import net.sf.iwant.api.javamodules.CodeStylePolicy;
import net.sf.iwant.api.javamodules.CodeStylePolicy.CodeStylePolicySpex;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Concatenated;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class EclipseProjectTest extends TestCase {

	private TestArea testArea;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private CachesMock caches;
	private TargetEvaluationContextMock evCtx;

	@Override
	protected void setUp() throws Exception {
		testArea = new EclipseSettingsTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.newDir("wsroot");
		caches = new CachesMock(wsRoot);
		evCtx = new TargetEvaluationContextMock(iwant, caches);
	}

	public void testSimpleSrcModuleDotProject() {
		JavaSrcModule module = JavaSrcModule.with().name("simple").end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotProject dotProject = project.eclipseDotProject();

		assertEquals("simple", dotProject.name());
	}

	public void testMinimalDotClasspath() {
		JavaSrcModule module = JavaSrcModule.with().name("simple").end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals("[]", dotClasspath.srcs().toString());
		assertEquals("[]", dotClasspath.deps().toString());
	}

	public void testDotClasspathWithMainJavaOnly() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals("[        <classpathentry kind=\"src\" path=\"src\"/>\n]",
				dotClasspath.srcs().toString());
		assertEquals("[]", dotClasspath.deps().toString());
	}

	public void testDotClasspathWithAllFourSrcs() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainResources("res").testJava("test")
				.testResources("testRes").end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals("[        <classpathentry kind=\"src\" path=\"src\"/>\n"
				+ ",         <classpathentry kind=\"src\" path=\"res\"/>\n"
				+ ",         <classpathentry kind=\"src\" path=\"test\"/>\n"
				+ ",         <classpathentry kind=\"src\" path=\"testRes\"/>\n"
				+ "]", dotClasspath.srcs().toString());
		assertEquals("[]", dotClasspath.deps().toString());
	}

	public void testDotClasspathWithMavenLayout() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mavenLayout().end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry kind=\"src\" path=\"src/main/java\"/>\n"
						+ ",         <classpathentry kind=\"src\" path=\"src/main/resources\"/>\n"
						+ ",         <classpathentry kind=\"src\" path=\"src/test/java\"/>\n"
						+ ",         <classpathentry kind=\"src\" path=\"src/test/resources\"/>\n"
						+ "]", dotClasspath.srcs().toString());
		assertEquals("[]", dotClasspath.deps().toString());
	}

	public void testDotClasspathWithOneDepToOtherSrcModule() {
		JavaSrcModule util = JavaSrcModule.with().name("util").mainJava("src")
				.end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(util).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/util\"/>\n]",
				dotClasspath.deps().toString());
	}

	public void testDotClasspathWithTwoSrcModulesAsMainDep() {
		JavaSrcModule util1 = JavaSrcModule.with().name("util1")
				.mainJava("src").end();
		JavaSrcModule util2 = JavaSrcModule.with().name("util2")
				.mainJava("src").end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(util1, util2).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/util1\"/>\n"
						+ ",         <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/util2\"/>\n"
						+ "]", dotClasspath.deps().toString());
	}

	public void testDotClasspathWithMainDepsAndTestDeps() {
		JavaSrcModule mainUtil = JavaSrcModule.with().name("main-util")
				.mainJava("src").end();
		JavaSrcModule testUtil = JavaSrcModule.with().name("test-util")
				.mainJava("src").end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(mainUtil).testDeps(testUtil).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/main-util\"/>\n"
						+ ",         <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/test-util\"/>\n"
						+ "]", dotClasspath.deps().toString());
	}

	public void testModuleThatIsBothMainDepAndSrcDepIsOnlyOnceInDotClasspath() {
		JavaSrcModule mainUtil = JavaSrcModule.with().name("main-util")
				.mainJava("src").end();
		JavaSrcModule testUtil = JavaSrcModule.with().name("test-util")
				.mainJava("src").end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(mainUtil)
				.testDeps(mainUtil, testUtil).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/main-util\"/>\n"
						+ ",         <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/test-util\"/>\n"
						+ "]", dotClasspath.deps().toString());
	}

	public void testDotClasspathWithOneDepToBinModuleProvidedBySrcModule() {
		JavaSrcModule libs = JavaSrcModule.with().name("libs").end();
		JavaModule util = JavaBinModule.named("util.jar").inside(libs);
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(util).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry kind=\"lib\" path=\"/libs/util.jar\"/>\n"
						+ "]", dotClasspath.deps().toString());
	}

	public void testDotClasspathWithOneDepToBinModuleWithSourcesProvidedBySrcModule() {
		JavaSrcModule libs = JavaSrcModule.with().name("libs").end();
		JavaModule util = JavaBinModule.named("util.jar")
				.source("util-src.zip").inside(libs);
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(util).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry kind=\"lib\" path=\"/libs/util.jar\" sourcepath=\"/libs/util-src.zip\"/>\n"
						+ "]", dotClasspath.deps().toString());
	}

	// code generation

	public void testDotProjectWithReferenceToCodeGenerator() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.pathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotProject dotProject = project.eclipseDotProject();

		assertTrue(dotProject.hasExternalBuilder());
	}

	public void testDotClasspathWithCodeGenerator() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.pathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry exported=\"true\" kind=\"lib\" path=\"eclipse-ant-generated/generated-classes\" sourcepath=\"eclipse-ant-generated/generated-src\"/>\n"
						+ "]", dotClasspath.deps().toString());
	}

	public void testProjectExternalBuilderLaunchIsNullWithoutCodeGeneration() {
		JavaSrcModule module = JavaSrcModule.with().name("simple").end();
		EclipseProject project = new EclipseProject(module, evCtx);

		assertNull(project.externalBuilderLaunch());
	}

	public void testProjectExternalBuilderLaunchHasCorrectContent() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.pathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		ProjectExternalBuilderLaunch launch = project.externalBuilderLaunch();

		assertEquals("code-generating-module", launch.name());
		assertEquals("eclipse-ant-generated", launch.relativeOutputDirectory());
		assertEquals("[]", launch.relativeInputPaths().toString());
	}

	public void testProjectExternalBuilderLaunchUsesGeneratorSourceAsRelativeInputPaths() {
		Source srcForGenerator = Source
				.underWsroot("gen-parent/gen-src-project/gen-src");
		Target generatedSrc = Concatenated.named("generated-src")
				.pathTo(srcForGenerator).end();
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.pathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module").relativeParentDir("parent-dir")
				.mainResources("src/main/resources")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		ProjectExternalBuilderLaunch launch = project.externalBuilderLaunch();

		assertEquals("code-generating-module", launch.name());
		assertEquals("eclipse-ant-generated", launch.relativeOutputDirectory());
		assertEquals("[gen-parent/gen-src-project/gen-src]", launch
				.relativeInputPaths().toString());
	}

	public void testProjectExternalBuilderLaunchUsesExplicitlyGivenRelativeInputPaths() {
		Source generatorMainJava = Source
				.underWsroot("gen-parent/generator/src/main/java");
		Source generatorMainResources = Source
				.underWsroot("gen-parent/generator/src/main/resources");
		Target generatorClasses = Concatenated.named("generator-classes")
				.string("In reality this is compiled from ")
				.pathTo(generatorMainJava).string(" and ")
				.pathTo(generatorMainResources).end();
		Target generatedSrc = Concatenated.named("generated-src")
				.string("In reality this is produced by running a class from ")
				.pathTo(generatorClasses).end();
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("In reality this is compiled from")
				.pathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule
				.with()
				.name("code-generating-module")
				.relativeParentDir("parent-dir")
				.mainResources("src/main/resources")
				.exportsClasses(generatedClasses, generatedSrc)
				.generatorSourcesToFollow(generatorMainJava,
						generatorMainResources).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		ProjectExternalBuilderLaunch launch = project.externalBuilderLaunch();

		assertEquals("code-generating-module", launch.name());
		assertEquals("eclipse-ant-generated", launch.relativeOutputDirectory());
		assertEquals(
				"[gen-parent/generator/src/main/java, gen-parent/generator/src/main/resources]",
				launch.relativeInputPaths().toString());
	}

	public void testEclipseAntScriptIsNullWithoutCodeGeneration() {
		JavaSrcModule module = JavaSrcModule.with().name("simple").end();
		EclipseProject project = new EclipseProject(module, evCtx);

		assertNull(project.eclipseAntScript("as-ws-developer"));
	}

	public void testEclipseAntScriptHasCorrectContent() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.pathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		EclipseAntScript antScript = project
				.eclipseAntScript("as-ws-developer");

		assertEquals("code-generating-module", antScript.projectName());
		assertEquals("..", antScript.relativeBasedir());
		assertEquals("", antScript.basedirRelativeParentDir());
		assertEquals("generated-src", antScript.srcTargetName());
		assertEquals("generated-classes", antScript.classesTargetName());
		assertEquals("as-ws-developer", antScript.asSomeone());
	}

	public void testEclipseAntScriptHasCorrectBasedirAndParentDirWhenModuleIsNotDirectlyUnderWsRoot() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.pathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.relativeParentDir("subdir1/subdir2")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		EclipseAntScript antScript = project
				.eclipseAntScript("as-ws-developer");

		assertEquals("../../..", antScript.relativeBasedir());
		assertEquals("subdir1/subdir2", antScript.basedirRelativeParentDir());
	}

	// compiler warnings

	public void testDefaultCompilerSettings() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").end();
		EclipseProject project = new EclipseProject(module, evCtx);

		OrgEclipseJdtCorePrefs prefs = project.orgEclipseJdtCorePrefs();

		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.deadCode=warning\n",
				prefs.asPropertyLine(CodeStyle.DEAD_CODE));
		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=ignore\n",
				prefs.asPropertyLine(CodeStyle.NON_EXTERNALIZED_STRING_LITERAL));
	}

	public void testOverriddenCompilerSettings() {
		CodeStylePolicySpex policy = CodeStylePolicy.defaultsExcept();
		policy.ignore(CodeStyle.DEAD_CODE);
		policy.warn(CodeStyle.NON_EXTERNALIZED_STRING_LITERAL);

		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").codeStyle(policy.end()).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		OrgEclipseJdtCorePrefs prefs = project.orgEclipseJdtCorePrefs();

		assertEquals("org.eclipse.jdt.core.compiler.problem.deadCode=ignore\n",
				prefs.asPropertyLine(CodeStyle.DEAD_CODE));
		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=warning\n",
				prefs.asPropertyLine(CodeStyle.NON_EXTERNALIZED_STRING_LITERAL));
	}

	// formatter

	public void testDefaultFormatterSettings() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").end();
		EclipseProject project = new EclipseProject(module, evCtx);

		OrgEclipseJdtCorePrefs prefs = project.orgEclipseJdtCorePrefs();
		CodeFormatterPolicy policy = prefs.codeFormatterPolicy();

		assertEquals(Integer.valueOf(0), policy.alignmentForEnumConstants);
	}

	public void testOverriddenFormatterSettings() {
		CodeFormatterPolicy policy = new CodeFormatterPolicy();
		policy.alignmentForEnumConstants = 48;

		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").codeFormatter(policy).end();
		EclipseProject project = new EclipseProject(module, evCtx);

		OrgEclipseJdtCorePrefs prefs = project.orgEclipseJdtCorePrefs();
		CodeFormatterPolicy policyAgain = prefs.codeFormatterPolicy();

		assertEquals(Integer.valueOf(48), policyAgain.alignmentForEnumConstants);
	}

	// formatter reference

	public void testProjectGivesUiPrefs() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").end();
		EclipseProject project = new EclipseProject(module, evCtx);

		OrgEclipseJdtUiPrefs uiPrefs = project.orgEclipseJdtUiPrefs();

		assertNotNull(uiPrefs);
	}

}
