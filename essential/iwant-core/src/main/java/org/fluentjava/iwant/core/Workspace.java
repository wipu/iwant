package org.fluentjava.iwant.core;

import java.util.SortedSet;
import java.util.TreeSet;

public class Workspace {

	private final String name;
	private final SortedSet<Target<?>> targets;

	private Workspace(String name, SortedSet<Target<?>> targets) {
		this.name = name;
		this.targets = targets;
	}

	public String name() {
		return name;
	}

	public SortedSet<Target<?>> targets() {
		return targets;
	}

	public static WorkspaceSpecification with() {
		return new WorkspaceSpecification();
	}

	public static class WorkspaceSpecification {

		private String name;
		private final SortedSet<Target<?>> targets = new TreeSet<Target<?>>();

		public WorkspaceSpecification name(String name) {
			this.name = name;
			return this;
		}

		public TargetSpecification target(String name) {
			return new TargetSpecification(name);
		}

		/**
		 * TODO combine with the other TargetBuilder
		 */
		public class TargetSpecification {

			private final String name;

			public TargetSpecification(String name) {
				this.name = name;
			}

			public WorkspaceSpecification content(Content content) {
				targets.add(new Target<Content>(name, content));
				return WorkspaceSpecification.this;
			}

		}

		public Workspace endWorkspace() {
			return new Workspace(name, targets);
		}

	}

}
