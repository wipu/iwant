package org.fluentjava.iwant.api.wsdef;

import java.util.Set;

import org.fluentjava.iwant.api.javamodules.JavaModule;

public interface WorkspaceModuleContext {

	Set<JavaModule> iwantApiModules();

	JavaModule wsdefdefModule();

	IwantPluginWishes iwantPlugin();

}
