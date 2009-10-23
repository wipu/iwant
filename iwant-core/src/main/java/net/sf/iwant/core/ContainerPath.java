package net.sf.iwant.core;

public class ContainerPath extends Path {

	public ContainerPath(String name) {
		super(name);
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
			return new Target(name, content);
		}

	}

	public TargetBuilder target(String name) {
		return new TargetBuilder().name(name);
	}

}
