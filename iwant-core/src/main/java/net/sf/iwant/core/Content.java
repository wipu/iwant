package net.sf.iwant.core;

import java.io.File;
import java.util.SortedSet;

public interface Content {

	SortedSet<Target> dependencies();

	void refresh(File destination) throws Exception;

}
