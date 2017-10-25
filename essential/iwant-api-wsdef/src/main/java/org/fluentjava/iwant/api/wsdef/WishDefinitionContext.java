package net.sf.iwant.api.wsdef;

import java.util.Set;

import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;

public interface WishDefinitionContext {

	JavaSrcModule wsdefdefJavaModule();

	JavaSrcModule wsdefJavaModule();

	Set<? extends JavaModule> iwantApiModules();

	IwantPluginWishes iwantPlugin();

}
