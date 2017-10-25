package net.sf.iwant.api.wsdef;

import net.sf.iwant.api.model.IngredientDefinitionContext;
import net.sf.iwant.api.model.SideEffectContext;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.api.model.WsRootProvider;

/**
 * If you cast a context to that, you need to be careful with the information
 * you are able to access. You need to know how to use it without breaking
 * contracts.
 */
public interface IKnowWhatIAmDoingContext extends TargetEvaluationContext,
		SideEffectContext, SideEffectDefinitionContext, TargetDefinitionContext,
		WorkspaceContext, IngredientDefinitionContext, WsRootProvider {

	// nothing to add

}
