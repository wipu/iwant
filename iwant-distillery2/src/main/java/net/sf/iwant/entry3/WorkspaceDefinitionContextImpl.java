package net.sf.iwant.entry3;

import java.util.Set;

import net.sf.iwant.api.WorkspaceDefinitionContext;
import net.sf.iwant.api.javamodules.JavaModule;

public class WorkspaceDefinitionContextImpl implements
		WorkspaceDefinitionContext {

	private Set<JavaModule> iwantApiModules;

	public WorkspaceDefinitionContextImpl(Set<JavaModule> iwantApiModules) {
		this.iwantApiModules = iwantApiModules;
	}

	@Override
	public Set<JavaModule> iwantApiModules() {
		return iwantApiModules;
	}

}
