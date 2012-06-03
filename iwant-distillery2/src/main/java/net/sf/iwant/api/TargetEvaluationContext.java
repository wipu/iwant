package net.sf.iwant.api;

import java.io.File;

import net.sf.iwant.entry.Iwant;

public interface TargetEvaluationContext {

	CacheLocations cached();

	File freshPathTo(Path path);

	Iwant iwant();

}
