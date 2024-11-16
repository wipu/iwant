package com.example.wsdef.editversionjacoco;

import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.api.wsdef.WorkspaceContext;
import org.fluentjava.iwant.api.wsdef.WorkspaceFactory;

import com.example.wsdef.editversioncustomtargetselfingr.IwanttutorialWorkspace;

public class IwanttutorialWorkspaceFactory implements WorkspaceFactory {

	@Override
	public Workspace workspace(WorkspaceContext ctx) {
		return new IwanttutorialWorkspace(ctx);
	}

}
