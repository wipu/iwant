package org.fluentjava.iwant.tests;

import org.junit.Test;

import org.fluentjava.iwant.api.javamodules.CodeFormatterPolicy.TabulationCharValue;
import org.fluentjava.iwant.api.javamodules.CodeStyle;
import org.fluentjava.iwant.api.javamodules.CodeStyleValue;
import org.fluentjava.iwant.api.javamodules.JavaCompliance;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics;
import org.fluentjava.iwant.core.download.FromRepository;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.embedded.AsEmbeddedIwantUser;
import org.fluentjava.iwant.iwantwsrootfinder.IwantWsRootFinder;
import org.fluentjava.iwant.plannerapi.TaskDirtiness;
import org.fluentjava.iwant.plugin.findbugs.FindbugsOutputFormat;
import org.fluentjava.iwant.plugin.github.FromGithub;

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
