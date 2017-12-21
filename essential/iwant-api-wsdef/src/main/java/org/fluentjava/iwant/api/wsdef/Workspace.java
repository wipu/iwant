package org.fluentjava.iwant.api.wsdef;

import java.util.List;

import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;

public interface Workspace {

	/**
	 * TODO SortedSet
	 */
	List<? extends Target> targets(TargetDefinitionContext ctx);

	List<? extends SideEffect> sideEffects(SideEffectDefinitionContext ctx);

}
