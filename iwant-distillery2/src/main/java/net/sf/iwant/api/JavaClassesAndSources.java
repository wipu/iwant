package net.sf.iwant.api;

public class JavaClassesAndSources {

	private Path classes;
	private Path sources;

	public JavaClassesAndSources(Path classes, Path sources) {
		this.classes = classes;
		this.sources = sources;
	}

	public String name() {
		return classes.name();
	}

	public Path classes() {
		return classes;
	}

	public Path sources() {
		return sources;
	}

}
