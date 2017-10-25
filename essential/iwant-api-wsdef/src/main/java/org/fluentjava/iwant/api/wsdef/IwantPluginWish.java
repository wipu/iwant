package org.fluentjava.iwant.api.wsdef;

import java.util.Set;

import org.fluentjava.iwant.api.javamodules.JavaModule;

public interface IwantPluginWish {

	Set<JavaModule> withDependencies();

}
