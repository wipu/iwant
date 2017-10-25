package net.sf.iwant.api.javamodules;

import java.util.Collections;
import java.util.List;

import net.sf.iwant.api.model.Path;

public class JavaClassesAndSources {

	private Path classes;
	private List<Path> sources;

	public JavaClassesAndSources(Path classes, Path source) {
		this(classes, Collections.singletonList(source));
	}

	public JavaClassesAndSources(Path classes, List<Path> sources) {
		this.classes = classes;
		this.sources = sources;
	}

	public String name() {
		return classes.name();
	}

	public Path classes() {
		return classes;
	}

	public List<Path> sources() {
		return sources;
	}

	@Override
	public String toString() {
		return "JavaClassesAndSources {" + classes() + " " + sources() + "}";
	}

}
