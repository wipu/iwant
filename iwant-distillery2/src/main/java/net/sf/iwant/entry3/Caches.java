package net.sf.iwant.entry3;

import java.io.File;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;

public interface Caches {

	File contentOf(Path path);

	File contentDescriptorOf(Target target);

	File temporaryDirectory(String workerName);

}
