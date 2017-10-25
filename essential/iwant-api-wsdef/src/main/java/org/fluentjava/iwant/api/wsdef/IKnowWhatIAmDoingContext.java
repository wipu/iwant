package org.fluentjava.iwant.api.wsdef;

import org.fluentjava.iwant.api.model.IngredientDefinitionContext;
import org.fluentjava.iwant.api.model.SideEffectContext;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.model.WsRootProvider;

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
