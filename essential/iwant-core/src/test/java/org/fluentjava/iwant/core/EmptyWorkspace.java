package net.sf.iwant.core;

public class EmptyWorkspace implements WorkspaceDefinition {

	private static class Root extends RootPath {

		public Root(Locations locations) {
			super(locations);
		}

	}

	@Override
	public ContainerPath wsRoot(Locations locations) {
		return new Root(locations);
	}

}