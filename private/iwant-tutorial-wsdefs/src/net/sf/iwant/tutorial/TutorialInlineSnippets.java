package net.sf.iwant.tutorial;

import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.plugin.ant.Jar;

public class TutorialInlineSnippets {

	@SuppressWarnings("unused")
	private Jar coolAppJar;
	private Source coolAppMainJava;
	private JavaClasses coolAppMainClasses;

	void coolAppJar() {
		coolAppJar = Jar.with().name("cool-app.jar")
				.classes(coolAppMainClasses).end();
	}

	void coolAppClasses() {
		coolAppMainClasses = JavaClasses.with().name("cool-app-main-classes")
				.srcDirs(coolAppMainJava).end();
	}

	void coolAppJava() {
		coolAppMainJava = Source.underWsroot("cool-app/src/main/java");
	}

}
