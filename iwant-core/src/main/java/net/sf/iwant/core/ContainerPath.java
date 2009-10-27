package net.sf.iwant.core;

public class ContainerPath extends Path {

	private final Locations locations;

	public ContainerPath(String name, Locations locations) {
		super(name);
		this.locations = locations;
	}

	public abstract class PathBuilder<B extends PathBuilder, P extends Path> {

		protected String name;
		protected Content content;

		public B name(String name) {
			this.name = name;
			return (B) this;
		}

		public abstract P end();

	}

	public class TargetBuilder extends PathBuilder<TargetBuilder, Target> {

		public TargetBuilder content(Content content) {
			this.content = content;
			return this;
		}

		@Override
		public Target end() {
			return new Target(name, locations, content);
		}

	}

	public TargetBuilder target(String name) {
		return new TargetBuilder().name(name);
	}

	public Source source(String name) {
		return new Source(name, locations);
	}

}
