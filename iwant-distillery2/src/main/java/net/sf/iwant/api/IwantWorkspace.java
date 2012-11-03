package net.sf.iwant.api;

import java.util.List;

public interface IwantWorkspace {

	/**
	 * TODO SortedSet
	 */
	List<? extends Target> targets();

	List<? extends SideEffect> sideEffects(SideEffectDefinitionContext ctx);

}
