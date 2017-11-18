package org.fluentjava.iwant.core;

public class AsDeveloper {

	public static WorkspaceWishes of(Workspace workspace) {
		return new WorkspaceWishes(workspace);
	}

	public static class WorkspaceWishes {

		private final Workspace ws;

		public WorkspaceWishes(Workspace ws) {
			this.ws = ws;
		}

		public LocatedWorkspaceWishes at(Locations locations) {
			return new LocatedWorkspaceWishes(locations);
		}

		public class LocatedWorkspaceWishes {

			private final Locations locations;

			public LocatedWorkspaceWishes(Locations locations) {
				this.locations = locations;
			}

			public LocatedWorkspaceWish iwant() {
				return new LocatedWorkspaceWish();
			}

			public class LocatedWorkspaceWish {

				public LocatedTargetWish target(String name) {
					Target<?> target = PathDigger.target(ws.targets(), name);
					return new LocatedTargetWish(target);
				}

			}

			public class LocatedTargetWish {

				private final Target<?> target;

				public LocatedTargetWish(Target<?> target) {
					this.target = target;
				}

				public String asPath() {
					return WorkspaceBuilder
							.freshTargetAsPath(target, locations);
				}

			}

		}

	}

}
