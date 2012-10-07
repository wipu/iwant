package net.sf.iwant.api;

import java.io.File;

import net.sf.iwant.entry.Iwant;

public interface TargetEvaluationContext {

	File modifiableTargets();

	File wsRoot();

	File cachedDescriptors();

	File freshPathTo(Path path);

	Iwant iwant();

}
