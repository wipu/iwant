package org.fluentjava.iwant.api.model;

import java.io.File;

public interface TargetEvaluationContext
		extends TemporaryDirectoryProvider, WsRootProvider {

	File cached(Path path);

	IwantCoreServices iwant();

}
