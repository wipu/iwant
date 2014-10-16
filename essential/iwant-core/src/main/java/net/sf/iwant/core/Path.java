package net.sf.iwant.core;

public abstract class Path implements Comparable<Path> {

	private final String name;

	public Path(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	@Override
	public int compareTo(Path o) {
		return name.compareTo(o.name);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Path))
			return false;
		Path o = (Path) obj;
		return name.equals(o.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + name;
	}

	public abstract String asAbsolutePath(Locations locations);

}
