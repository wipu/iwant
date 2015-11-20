package net.sf.iwant.tests;

import org.junit.Test;

import net.sf.iwant.api.javamodules.CodeFormatterPolicy.TabulationCharValue;
import net.sf.iwant.api.javamodules.CodeStyle;
import net.sf.iwant.api.javamodules.CodeStyleValue;
import net.sf.iwant.api.javamodules.JavaCompliance;
import net.sf.iwant.api.javamodules.StandardCharacteristics;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.embedded.AsEmbeddedIwantUser;
import net.sf.iwant.iwantwsrootfinder.IwantWsRootFinder;
import net.sf.iwant.plannerapi.TaskDirtiness;
import net.sf.iwant.plugin.findbugs.FindbugsOutputFormat;
import net.sf.iwant.plugin.github.FromGithub;

public class SyntheticCodeTest {

	@Test
	@SuppressWarnings("unused")
	public void utilityClassesCanBeInstantiatedButItIsUseless() {
		new AsEmbeddedIwantUser();
		new IwantWsRootFinder();
		new FromGithub();
		new FromRepository();
		new StandardCharacteristics();
		new TestedIwantDependencies();
	}

	@Test
	public void syntheticEnumMethods() {
		CodeStyle.valueOf("DEAD_CODE");
		CodeStyle.values();

		CodeStyleValue.valueOf("WARN");
		CodeStyleValue.values();

		FindbugsOutputFormat.valueOf("HTML");
		FindbugsOutputFormat.values();

		JavaCompliance.valueOf("JAVA_1_8");
		JavaCompliance.values();

		TabulationCharValue.valueOf("SPACE");
		TabulationCharValue.values();

		TaskDirtiness.valueOf("NOT_DIRTY");
		TaskDirtiness.values();
	}

}
