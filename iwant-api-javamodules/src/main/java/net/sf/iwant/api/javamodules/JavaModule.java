package net.sf.iwant.api.javamodules;

import net.sf.iwant.api.model.Path;

public abstract class JavaModule implements Comparable<JavaModule> {

	public abstract String name();

	public abstract Path mainArtifact();

	@Override
	public int compareTo(JavaModule o) {
		return name().compareTo(o.name());
	}

}
