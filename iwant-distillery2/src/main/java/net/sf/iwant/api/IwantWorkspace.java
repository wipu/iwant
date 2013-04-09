package net.sf.iwant.api;

import java.util.List;

import net.sf.iwant.api.model.Target;

public interface IwantWorkspace {

	/**
	 * TODO SortedSet
	 */
	List<? extends Target> targets();

	List<? extends SideEffect> sideEffects(SideEffectDefinitionContext ctx);

}
