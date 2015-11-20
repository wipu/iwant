package net.sf.iwant.tutorial;

import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.plugin.ant.Jar;

public class TutorialInlineSnippets {

	/* snippet-start coolAppMainJava */
	Source coolAppMainJava = Source.underWsroot("cool-app/src/main/java");
	/* snippet-end coolAppMainJava */

	/* snippet-start coolAppMainClasses */
	Target coolAppMainClasses = JavaClasses.with().name("cool-app-main-classes")
			.srcDirs(coolAppMainJava).end();
			/* snippet-end coolAppMainClasses */

	/* snippet-start coolAppJar */
	Target coolAppJar = Jar.with().name("cool-app.jar")
			.classes(coolAppMainClasses).end();
	/* snippet-end coolAppJar */

}
