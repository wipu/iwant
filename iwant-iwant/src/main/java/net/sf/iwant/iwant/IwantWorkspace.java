package net.sf.iwant.iwant;

import net.sf.iwant.core.ContainerPath;
import net.sf.iwant.core.Locations;
import net.sf.iwant.core.RootPath;
import net.sf.iwant.core.WorkspaceDefinition;

public class IwantWorkspace implements WorkspaceDefinition {

	@Override
	public ContainerPath wsRoot(Locations locations) {
		return new Root(locations);
	}

	public static class Root extends RootPath {

		public Root(Locations locations) {
			super(locations);
		}

	}

}
