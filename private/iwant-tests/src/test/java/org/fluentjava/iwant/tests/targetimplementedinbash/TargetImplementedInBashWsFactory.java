package net.sf.iwant.tests.targetimplementedinbash;

import net.sf.iwant.api.wsdef.Workspace;
import net.sf.iwant.api.wsdef.WorkspaceContext;
import net.sf.iwant.api.wsdef.WorkspaceFactory;

public class TargetImplementedInBashWsFactory implements WorkspaceFactory {

	@Override
	public Workspace workspace(WorkspaceContext ctx) {
		return new TargetImplementedInBashWsdef();
	}

}
