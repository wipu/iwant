package net.sf.iwant.api;

import java.util.Set;

import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;

public interface SideEffectDefinitionContext {

	JavaSrcModule wsdefdefJavaModule();

	JavaSrcModule wsdefJavaModule();

	Set<? extends JavaModule> iwantApiModules();

	IwantPluginWishes iwantPlugin();

}
