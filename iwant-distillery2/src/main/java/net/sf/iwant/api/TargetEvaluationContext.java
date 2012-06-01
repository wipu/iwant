package net.sf.iwant.api;

import java.io.File;

import net.sf.iwant.entry.Iwant;

public interface TargetEvaluationContext {

	File wsRoot();

	File freshPathTo(Target target);

	Iwant iwant();

}
