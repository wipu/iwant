package net.sf.iwant.api;

import java.io.File;

import net.sf.iwant.entry.Iwant;

public interface TargetEvaluationContext {

	File wsRoot();

	File cached(Path path);

	Iwant iwant();

	File freshTemporaryDirectory();

}
