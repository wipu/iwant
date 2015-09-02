package net.sf.iwant.api.wsdef;

import java.util.List;

import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;

public interface Workspace {

	/**
	 * TODO SortedSet
	 */
	List<? extends Target> targets(TargetDefinitionContext ctx);

	List<? extends SideEffect> sideEffects(SideEffectDefinitionContext ctx);

}
