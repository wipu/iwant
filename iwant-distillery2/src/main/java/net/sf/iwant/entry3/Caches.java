package net.sf.iwant.entry3;

import java.io.File;

import net.sf.iwant.api.Path;
import net.sf.iwant.api.Target;

public interface Caches {

	File contentOf(Path path);

	File contentDescriptorOf(Target target);

}
