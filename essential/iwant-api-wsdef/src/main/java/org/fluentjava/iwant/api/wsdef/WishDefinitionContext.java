package org.fluentjava.iwant.api.wsdef;

import java.util.Set;

import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;

public interface WishDefinitionContext {

	JavaSrcModule wsdefdefJavaModule();

	JavaSrcModule wsdefJavaModule();

	Set<? extends JavaModule> iwantApiModules();

	IwantPluginWishes iwantPlugin();

}
