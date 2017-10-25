package net.sf.iwant.tests.targetimplementedinbash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.bash.TargetImplementedInBash;
import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.api.wsdef.Workspace;

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
