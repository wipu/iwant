package org.fluentjava.iwant.tests.targetimplementedinbash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.bash.TargetImplementedInBash;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;

public class TargetImplementedInBashWsdef implements Workspace {

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		List<Target> targets = new ArrayList<>();
		targets.add(new HelloTarget("target-ingr", "content of target-ingr"));
		targets.addAll(
				TargetImplementedInBash.instancesFromDefaultIndexSh(ctx));
		return targets;
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList();
	}

}
