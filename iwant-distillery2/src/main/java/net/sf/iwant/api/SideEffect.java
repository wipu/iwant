package net.sf.iwant.api;

public interface SideEffect {

	String name();

	void mutate(SideEffectContext ctx) throws Exception;

}
