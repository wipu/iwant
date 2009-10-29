package net.sf.iwant.core;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;

public interface Content {

	SortedSet<Target> dependencies();

	void refresh(File destination) throws IOException;

}
