package net.sf.iwant.api.model;

import java.io.File;

public interface TargetEvaluationContext {

	File wsRoot();

	File cached(Path path);

	IwantCoreServices iwant();

	File freshTemporaryDirectory();

}
