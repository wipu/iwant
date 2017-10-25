package net.sf.iwant.api.wsdef;

import java.util.Set;

import net.sf.iwant.api.javamodules.JavaModule;

public interface IwantPluginWish {

	Set<JavaModule> withDependencies();

}
