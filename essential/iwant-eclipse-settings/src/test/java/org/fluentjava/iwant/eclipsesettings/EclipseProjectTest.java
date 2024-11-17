package org.fluentjava.iwant.eclipsesettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.CodeFormatterPolicy;
import org.fluentjava.iwant.api.javamodules.CodeStyle;
import org.fluentjava.iwant.api.javamodules.CodeStylePolicy;
import org.fluentjava.iwant.api.javamodules.CodeStylePolicy.CodeStylePolicySpex;
import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.javamodules.ScalaVersion;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.apimocks.TargetMock;
import org.junit.jupiter.api.Test;

public class EclipseProjectTest extends IwantTestCase {

	@Test
	public void simpleSrcModuleDotProject() {
		JavaSrcModule module = JavaSrcModule.with().name("simple").end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotProject dotProject = project.eclipseDotProject();

		assertEquals("simple", dotProject.name());
	}

	@Test
	public void minimalDotClasspath() {
		JavaSrcModule module = JavaSrcModule.with().name("simple").end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals("[]", dotClasspath.srcs().toString());
		assertEquals("[]", dotClasspath.deps().toString());
	}

	@Test
	public void dotClasspathWithMainJavaOnly() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals("[        <classpathentry kind=\"src\" path=\"src\"/>\n]",
				dotClasspath.srcs().toString());
		assertEquals("[]", dotClasspath.deps().toString());
	}

	@Test
	public void dotClasspathWithAllFourSrcs() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainResources("res").testJava("test")
				.testResources("testRes").end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals("[        <classpathentry kind=\"src\" path=\"test\"/>\n"
				+ ",         <classpathentry kind=\"src\" path=\"testRes\"/>\n"
				+ ",         <classpathentry kind=\"src\" path=\"src\"/>\n"
				+ ",         <classpathentry kind=\"src\" path=\"res\"/>\n"
				+ "]", dotClasspath.srcs().toString());
		assertEquals("[]", dotClasspath.deps().toString());
	}

	@Test
	public void dotClasspathWithMavenLayout() {
		JavaSrcModule module = JavaSrcModule.with().name("simple").mavenLayout()
				.end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry kind=\"src\" path=\"src/test/java\"/>\n"
						+ ",         <classpathentry kind=\"src\" path=\"src/test/resources\"/>\n"
						+ ",         <classpathentry kind=\"src\" path=\"src/main/java\"/>\n"
						+ ",         <classpathentry kind=\"src\" path=\"src/main/resources\"/>\n"
						+ "]",
				dotClasspath.srcs().toString());
		assertEquals("[]", dotClasspath.deps().toString());
	}

	@Test
	public void dotClasspathWithOneDepToOtherSrcModule() {
		JavaSrcModule util = JavaSrcModule.with().name("util").mainJava("src")
				.end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(util).end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/util\"/>\n]",
				dotClasspath.deps().toString());
	}

	@Test
	public void dotClasspathWithTwoSrcModulesAsMainDep() {
		JavaSrcModule util1 = JavaSrcModule.with().name("util1").mainJava("src")
				.end();
		JavaSrcModule util2 = JavaSrcModule.with().name("util2").mainJava("src")
				.end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(util1, util2).end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/util1\"/>\n"
						+ ",         <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/util2\"/>\n"
						+ "]",
				dotClasspath.deps().toString());
	}

	@Test
	public void dotClasspathWithMainDepsAndTestDeps() {
		JavaSrcModule mainUtil = JavaSrcModule.with().name("main-util")
				.mainJava("src").end();
		JavaSrcModule testUtil = JavaSrcModule.with().name("test-util")
				.mainJava("src").end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(mainUtil).testDeps(testUtil).end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/test-util\"/>\n"
						+ ",         <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/main-util\"/>\n"
						+ "]",
				dotClasspath.deps().toString());
	}

	@Test
	public void moduleThatIsBothMainDepAndSrcDepIsOnlyOnceInDotClasspath() {
		JavaSrcModule mainUtil = JavaSrcModule.with().name("main-util")
				.mainJava("src").end();
		JavaSrcModule testUtil = JavaSrcModule.with().name("test-util")
				.mainJava("src").end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(mainUtil).testDeps(mainUtil, testUtil)
				.end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/main-util\"/>\n"
						+ ",         <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/test-util\"/>\n"
						+ "]",
				dotClasspath.deps().toString());
	}

	@Test
	public void dotClasspathWithOneDepToBinModuleProvidedBySrcModule() {
		JavaSrcModule libs = JavaSrcModule.with().name("libs").end();
		JavaModule util = JavaBinModule.named("util.jar").inside(libs).end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(util).end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry kind=\"lib\" path=\"/libs/util.jar\"/>\n"
						+ "]",
				dotClasspath.deps().toString());
	}

	@Test
	public void dotClasspathWithOneDepToBinModuleWithSourcesProvidedBySrcModule() {
		JavaSrcModule libs = JavaSrcModule.with().name("libs").end();
		JavaModule util = JavaBinModule.named("util.jar").source("util-src.zip")
				.inside(libs).end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").mainDeps(util).end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry kind=\"lib\" path=\"/libs/util.jar\" sourcepath=\"/libs/util-src.zip\"/>\n"
						+ "]",
				dotClasspath.deps().toString());
	}

	@Test
	public void dotClasspathIncludesRuntimeDepOfTestRuntimeBinaryDep() {
		JavaBinModule depOfBinDep = JavaBinModule
				.providing(new TargetMock("depOfBinDep.jar")).end();
		JavaBinModule binDep = JavaBinModule
				.providing(new TargetMock("binDep.jar"))
				.runtimeDeps(depOfBinDep).end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").testRuntimeDeps(binDep).end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry kind=\"lib\" path=\""
						+ slashed(cached) + "/binDep.jar\"/>\n"
						+ ",         <classpathentry kind=\"lib\" path=\""
						+ slashed(cached) + "/depOfBinDep.jar\"/>\n" + "]",
				dotClasspath.deps().toString());
	}

	// code generation

	@Test
	public void dotProjectWithReferenceToCodeGenerator() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.nativePathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotProject dotProject = project.eclipseDotProject();

		assertTrue(dotProject.hasExternalBuilder());
	}

	@Test
	public void dotClasspathWithCodeGenerator() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.nativePathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotClasspath dotClasspath = project.eclipseDotClasspath();

		assertEquals(
				"[        <classpathentry exported=\"true\" kind=\"lib\" path=\"eclipse-ant-generated/generated-classes\" sourcepath=\"eclipse-ant-generated/generated-src\"/>\n"
						+ "]",
				dotClasspath.deps().toString());
	}

	@Test
	public void projectExternalBuilderLaunchIsNullWithoutCodeGeneration() {
		JavaSrcModule module = JavaSrcModule.with().name("simple").end();
		EclipseProject project = new EclipseProject(module, ctx);

		assertNull(project.externalBuilderLaunch());
	}

	@Test
	public void projectExternalBuilderLaunchHasCorrectContent() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.nativePathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, ctx);

		ProjectExternalBuilderLaunch launch = project.externalBuilderLaunch();

		assertEquals("code-generating-module", launch.name());
		assertEquals("eclipse-ant-generated", launch.relativeOutputDirectory());
		assertEquals("[]", launch.relativeInputPaths().toString());
	}

	@Test
	public void projectExternalBuilderLaunchUsesGeneratorSourceAsRelativeInputPaths() {
		Source srcForGenerator = Source
				.underWsroot("gen-parent/gen-src-project/gen-src");
		Target generatedSrc = Concatenated.named("generated-src")
				.nativePathTo(srcForGenerator).end();
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.nativePathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module").relativeParentDir("parent-dir")
				.mainResources("src/main/resources")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, ctx);

		ProjectExternalBuilderLaunch launch = project.externalBuilderLaunch();

		assertEquals("code-generating-module", launch.name());
		assertEquals("eclipse-ant-generated", launch.relativeOutputDirectory());
		assertEquals("[gen-parent/gen-src-project/gen-src]",
				launch.relativeInputPaths().toString());
	}

	@Test
	public void projectExternalBuilderLaunchUsesExplicitlyGivenRelativeInputPaths() {
		Source generatorMainJava = Source
				.underWsroot("gen-parent/generator/src/main/java");
		Source generatorMainResources = Source
				.underWsroot("gen-parent/generator/src/main/resources");
		Target generatorClasses = Concatenated.named("generator-classes")
				.string("In reality this is compiled from ")
				.nativePathTo(generatorMainJava).string(" and ")
				.nativePathTo(generatorMainResources).end();
		Target generatedSrc = Concatenated.named("generated-src")
				.string("In reality this is produced by running a class from ")
				.nativePathTo(generatorClasses).end();
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("In reality this is compiled from")
				.nativePathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module").relativeParentDir("parent-dir")
				.mainResources("src/main/resources")
				.exportsClasses(generatedClasses, generatedSrc)
				.generatorSourcesToFollow(generatorMainJava,
						generatorMainResources)
				.end();
		EclipseProject project = new EclipseProject(module, ctx);

		ProjectExternalBuilderLaunch launch = project.externalBuilderLaunch();

		assertEquals("code-generating-module", launch.name());
		assertEquals("eclipse-ant-generated", launch.relativeOutputDirectory());
		assertEquals(
				"[gen-parent/generator/src/main/java, gen-parent/generator/src/main/resources]",
				launch.relativeInputPaths().toString());
	}

	@Test
	public void eclipseAntScriptIsNullWithoutCodeGeneration() {
		JavaSrcModule module = JavaSrcModule.with().name("simple").end();
		EclipseProject project = new EclipseProject(module, ctx);

		assertNull(project.eclipseAntScript("as-ws-developer"));
	}

	@Test
	public void eclipseAntScriptHasCorrectContent() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.nativePathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, ctx);

		EclipseAntScript antScript = project
				.eclipseAntScript("as-ws-developer");

		assertEquals("code-generating-module", antScript.projectName());
		assertEquals("..", antScript.relativeBasedir());
		assertEquals("", antScript.basedirRelativeParentDir());
		assertEquals("generated-src", antScript.srcTargetName());
		assertEquals("generated-classes", antScript.classesTargetName());
		assertEquals("as-ws-developer", antScript.asSomeone());
	}

	@Test
	public void eclipseAntScriptHasCorrectBasedirAndParentDirWhenModuleIsNotDirectlyUnderWsRoot() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.nativePathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.relativeParentDir("subdir1/subdir2")
				.exportsClasses(generatedClasses, generatedSrc).end();
		EclipseProject project = new EclipseProject(module, ctx);

		EclipseAntScript antScript = project
				.eclipseAntScript("as-ws-developer");

		assertEquals("../../..", antScript.relativeBasedir());
		assertEquals("subdir1/subdir2", antScript.basedirRelativeParentDir());
	}

	// compiler warnings

	@Test
	public void defaultCompilerSettings() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").end();
		EclipseProject project = new EclipseProject(module, ctx);

		OrgEclipseJdtCorePrefs prefs = project.orgEclipseJdtCorePrefs();

		assertEquals("org.eclipse.jdt.core.compiler.problem.deadCode=warning\n",
				prefs.asPropertyLine(CodeStyle.DEAD_CODE));
		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=ignore\n",
				prefs.asPropertyLine(
						CodeStyle.NON_EXTERNALIZED_STRING_LITERAL));
	}

	@Test
	public void overriddenCompilerSettings() {
		CodeStylePolicySpex policy = CodeStylePolicy.defaultsExcept();
		policy.ignore(CodeStyle.DEAD_CODE);
		policy.warn(CodeStyle.NON_EXTERNALIZED_STRING_LITERAL);

		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").codeStyle(policy.end()).end();
		EclipseProject project = new EclipseProject(module, ctx);

		OrgEclipseJdtCorePrefs prefs = project.orgEclipseJdtCorePrefs();

		assertEquals("org.eclipse.jdt.core.compiler.problem.deadCode=ignore\n",
				prefs.asPropertyLine(CodeStyle.DEAD_CODE));
		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=warning\n",
				prefs.asPropertyLine(
						CodeStyle.NON_EXTERNALIZED_STRING_LITERAL));
	}

	// formatter

	@Test
	public void defaultFormatterSettings() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").end();
		EclipseProject project = new EclipseProject(module, ctx);

		OrgEclipseJdtCorePrefs prefs = project.orgEclipseJdtCorePrefs();
		CodeFormatterPolicy policy = prefs.codeFormatterPolicy();

		assertEquals(Integer.valueOf(0), policy.alignmentForEnumConstants);
	}

	@Test
	public void overriddenFormatterSettings() {
		CodeFormatterPolicy policy = new CodeFormatterPolicy();
		policy.alignmentForEnumConstants = 48;

		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").codeFormatter(policy).end();
		EclipseProject project = new EclipseProject(module, ctx);

		OrgEclipseJdtCorePrefs prefs = project.orgEclipseJdtCorePrefs();
		CodeFormatterPolicy policyAgain = prefs.codeFormatterPolicy();

		assertEquals(Integer.valueOf(48),
				policyAgain.alignmentForEnumConstants);
	}

	// formatter reference

	@Test
	public void projectGivesUiPrefs() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").end();
		EclipseProject project = new EclipseProject(module, ctx);

		OrgEclipseJdtUiPrefs uiPrefs = project.orgEclipseJdtUiPrefs();

		assertNotNull(uiPrefs);
	}

	@Test
	public void dotProjectHasScalaSupportWhenEnabled() {
		JavaSrcModule module = JavaSrcModule.with().name("mixed")
				.scalaVersion(ScalaVersion._2_11_7()).mainJava("src/main/java")
				.mainScala("src/main/scala").end();
		EclipseProject project = new EclipseProject(module, ctx);

		DotProject dotProject = project.eclipseDotProject();
		assertTrue(dotProject.hasScalaSupport());
	}

}
