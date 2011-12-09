package net.sf.iwant.core;

import java.util.SortedSet;

public class ContainerPath extends Path {

	protected final Locations locations;

	public ContainerPath(String name, Locations locations) {
		super(name);
		this.locations = locations;
	}

	public abstract class PathBuilder<B extends PathBuilder<?, P>, P extends Path> {

		protected String name;
		protected Content content;

		@SuppressWarnings("unchecked")
		public B name(String name) {
			this.name = name;
			return (B) this;
		}

		public abstract P end();

	}

	public class TargetName {

		public TargetBuilder name(String name) {
			return new TargetBuilder(name);
		}

	}

	public class TargetBuilder {

		private final String name;

		public TargetBuilder(String name) {
			this.name = name;
		}

		public <CONTENT extends Content> TargetEnd<CONTENT> content(
				CONTENT content) {
			return new TargetEnd<CONTENT>(name, content);
		}

	}

	public class TargetEnd<CONTENT extends Content> {

		private final String name;
		private final CONTENT content;

		public TargetEnd(String name, CONTENT content) {
			this.name = name;
			this.content = content;
		}

		public Target<CONTENT> end() {
			return new Target<CONTENT>(name, content);
		}

	}

	public TargetBuilder target(String name) {
		return new TargetName().name(name);
	}

	public Source source(String name) {
		return new Source(name);
	}

	protected Builtins builtin() {
		return new Builtins(locations);
	}

	@Override
	public String asAbsolutePath(Locations locations) {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	/**
	 * Override if automatic target digging by reflection is not what you want.
	 */
	public SortedSet<Target<?>> targets() {
		return PathDigger.targets(this);
	}

}
