package org.fluentjava.iwant.api.model;

public interface SideEffect {

	String name();

	void mutate(SideEffectContext ctx) throws Exception;

}
