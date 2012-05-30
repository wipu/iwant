package net.sf.iwant.api;

import java.io.File;

public interface TargetEvaluationContext {

	File wsRoot();

	File freshPathTo(Target target);

}
