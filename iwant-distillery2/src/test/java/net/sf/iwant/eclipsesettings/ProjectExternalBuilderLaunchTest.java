package net.sf.iwant.eclipsesettings;

import java.util.Arrays;

import junit.framework.TestCase;
import net.sf.iwant.api.model.Concatenated;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;

public class ProjectExternalBuilderLaunchTest extends TestCase {

	public void testGettersOfMinimal() {
		Target generatedJava = new HelloTarget("genSrc", "");
		ProjectExternalBuilderLaunch launch = new ProjectExternalBuilderLaunch(
				"project-name", generatedJava, Arrays.<Source> asList(),
				"generator-out");

		assertEquals("project-name", launch.name());
		assertEquals("[]", launch.relativeInputPaths().toString());
		assertEquals("generator-out", launch.relativeOutputDirectory());
	}

	public void testSourceIngredientsOfGeneratedJavaAreUsedAsGeneratorInputs() {
		Path ingr1ForGenerator = new HelloTarget("target-ingr1", "");
		Path ingr2ForGenerator = Source.underWsroot("src-ingr1");
		Path ingr3ForGenerator = new HelloTarget("target-ingr2", "");
		Path ingr4ForGenerator = Source.underWsroot("src/ingr2");

		Target generatedJava = Concatenated.named("generated-java")
				.contentOf(ingr1ForGenerator).contentOf(ingr2ForGenerator)
				.contentOf(ingr3ForGenerator).contentOf(ingr4ForGenerator)
				.end();

		ProjectExternalBuilderLaunch launch = new ProjectExternalBuilderLaunch(
				"project-name", generatedJava, Arrays.<Source> asList(),
				"generator-out");

		assertEquals("[src-ingr1, src/ingr2]", launch.relativeInputPaths()
				.toString());
	}

	public void testSourceIngredientsOfGeneratedJavaAreUsedAsGeneratorInputsTogetherWithExplicitlyGivenInputs() {
		Source genSrc1 = Source.underWsroot("explicit-gen-src1");
		Source genSrc2 = Source.underWsroot("explicit-gen-src2");

		Path ingr1ForGenerator = new HelloTarget("target-ingr1", "");
		Path ingr2ForGenerator = Source.underWsroot("src-ingr1");
		Path ingr3ForGenerator = new HelloTarget("target-ingr2", "");
		Path ingr4ForGenerator = Source.underWsroot("src/ingr2");

		Target generatedJava = Concatenated.named("generated-java")
				.contentOf(ingr1ForGenerator).contentOf(ingr2ForGenerator)
				.contentOf(ingr3ForGenerator).contentOf(ingr4ForGenerator)
				.end();

		ProjectExternalBuilderLaunch launch = new ProjectExternalBuilderLaunch(
				"project-name", generatedJava, Arrays.asList(genSrc1, genSrc2),
				"generator-out");

		assertEquals(
				"[explicit-gen-src1, explicit-gen-src2, src-ingr1, src/ingr2]",
				launch.relativeInputPaths().toString());
	}

	/**
	 * TODO this hasn't been tested in real life, and it most probably doesn't
	 * even make sense so maybe we should require ingredients.
	 */
	public void testFileContentOfLaunchWithNoSrcIngredients() {
		Concatenated generatedJava = Concatenated.named("genSrc")
				.string("no ingredients").end();
		ProjectExternalBuilderLaunch launch = new ProjectExternalBuilderLaunch(
				"project-name-2", generatedJava, Arrays.<Source> asList(),
				"generator-out-2");

		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		expected.append("<launchConfiguration type=\"org.eclipse.ant.AntBuilderLaunchConfigurationType\">\n");
		expected.append("<stringAttribute key=\"org.eclipse.ant.ui.ATTR_ANT_CLEAN_TARGETS\" value=\"project-clean,\"/>\n");
		expected.append("<booleanAttribute key=\"org.eclipse.ant.ui.ATTR_TARGETS_UPDATED\" value=\"true\"/>\n");
		expected.append("<booleanAttribute key=\"org.eclipse.ant.ui.DEFAULT_VM_INSTALL\" value=\"false\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.debug.core.ATTR_REFRESH_SCOPE\" value=\"${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;&lt;item path=&quot;/project-name-2/generator-out-2&quot; type=&quot;2&quot;/&gt;&#10;&lt;/resources&gt;}\"/>\n");
		expected.append("<booleanAttribute key=\"org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND\" value=\"false\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.jdt.launching.CLASSPATH_PROVIDER\" value=\"org.eclipse.ant.ui.AntClasspathProvider\"/>\n");
		expected.append("<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"true\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.jdt.launching.JRE_CONTAINER\" value=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/CDC-1.0%Foundation-1.0\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.jdt.launching.MAIN_TYPE\" value=\"org.eclipse.ant.internal.launching.remote.InternalAntRunner\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\"project-name-2\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_BUILD_SCOPE\" value=\"${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;&lt;/resources&gt;}\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LOCATION\" value=\"${workspace_loc:/project-name-2/eclipse-ant-build.xml}\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_RUN_BUILD_KINDS\" value=\"full,incremental,auto,clean\"/>\n");
		expected.append("<booleanAttribute key=\"org.eclipse.ui.externaltools.ATTR_TRIGGERS_CONFIGURED\" value=\"true\"/>\n");
		expected.append("<stringAttribute key=\"process_factory_id\" value=\"org.eclipse.ant.ui.remoteAntProcessFactory\"/>\n");
		expected.append("</launchConfiguration>\n");

		assertEquals(expected.toString(), launch.asFileContent());
	}

	public void testFileContentOfLaunchWithManySrcIngredients() {
		Concatenated generatedJava = Concatenated.named("genSrc")
				.contentOf(Source.underWsroot("module-a/src-for-generator"))
				.contentOf(Source.underWsroot("module-b/src-for-generator"))
				.end();
		ProjectExternalBuilderLaunch launch = new ProjectExternalBuilderLaunch(
				"project-name-2", generatedJava, Arrays.<Source> asList(),
				"generator-out-2");

		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		expected.append("<launchConfiguration type=\"org.eclipse.ant.AntBuilderLaunchConfigurationType\">\n");
		expected.append("<stringAttribute key=\"org.eclipse.ant.ui.ATTR_ANT_CLEAN_TARGETS\" value=\"project-clean,\"/>\n");
		expected.append("<booleanAttribute key=\"org.eclipse.ant.ui.ATTR_TARGETS_UPDATED\" value=\"true\"/>\n");
		expected.append("<booleanAttribute key=\"org.eclipse.ant.ui.DEFAULT_VM_INSTALL\" value=\"false\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.debug.core.ATTR_REFRESH_SCOPE\" value=\"${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;&lt;item path=&quot;/project-name-2/generator-out-2&quot; type=&quot;2&quot;/&gt;&#10;&lt;/resources&gt;}\"/>\n");
		expected.append("<booleanAttribute key=\"org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND\" value=\"false\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.jdt.launching.CLASSPATH_PROVIDER\" value=\"org.eclipse.ant.ui.AntClasspathProvider\"/>\n");
		expected.append("<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"true\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.jdt.launching.JRE_CONTAINER\" value=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/CDC-1.0%Foundation-1.0\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.jdt.launching.MAIN_TYPE\" value=\"org.eclipse.ant.internal.launching.remote.InternalAntRunner\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\"project-name-2\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_BUILD_SCOPE\" value=\"${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;&lt;item path=&quot;/module-a/src-for-generator&quot; type=&quot;2&quot;/&gt;&#10;&lt;item path=&quot;/module-b/src-for-generator&quot; type=&quot;2&quot;/&gt;&#10;&lt;/resources&gt;}\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LOCATION\" value=\"${workspace_loc:/project-name-2/eclipse-ant-build.xml}\"/>\n");
		expected.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_RUN_BUILD_KINDS\" value=\"full,incremental,auto,clean\"/>\n");
		expected.append("<booleanAttribute key=\"org.eclipse.ui.externaltools.ATTR_TRIGGERS_CONFIGURED\" value=\"true\"/>\n");
		expected.append("<stringAttribute key=\"process_factory_id\" value=\"org.eclipse.ant.ui.remoteAntProcessFactory\"/>\n");
		expected.append("</launchConfiguration>\n");

		assertEquals(expected.toString(), launch.asFileContent());
	}

}
