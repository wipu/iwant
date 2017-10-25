package org.fluentjava.iwant.api.model;

import java.io.File;

/**
 * TODO move this to another "internal api" module, users don't really need this
 */
public interface Caches {

	File contentOf(Path path);

	File contentDescriptorOf(Target target);

	File temporaryDirectory(String workerName);

}
