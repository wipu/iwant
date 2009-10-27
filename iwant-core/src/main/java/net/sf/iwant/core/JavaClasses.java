package net.sf.iwant.core;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;

public class JavaClasses implements Content {

	private final Source src;

	public JavaClasses(Source src) {
		this.src = src;
	}

	public static JavaClasses compiledFrom(Source src) {
		return new JavaClasses(src);
	}

	public void refresh(File destination) {
		destination.mkdir();
		Project project = new Project();
		Javac javac = new Javac();
		javac.setProject(project);
		javac
				.setSrcdir(new org.apache.tools.ant.types.Path(project, src
						.name()));
		javac.setDestdir(destination);
		javac.setFork(true);
		javac.setDebug(true);
		javac.execute();

	}

}
