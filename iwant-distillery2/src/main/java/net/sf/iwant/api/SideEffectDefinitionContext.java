package net.sf.iwant.api;

import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;

public interface SideEffectDefinitionContext {

	JavaSrcModule wsdefdefJavaModule();

	JavaSrcModule wsdefJavaModule();

	JavaModule[] iwantApiModules();

}
