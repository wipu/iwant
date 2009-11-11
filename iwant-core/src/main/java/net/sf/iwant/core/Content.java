package net.sf.iwant.core;

import java.io.File;
import java.util.SortedSet;

public interface Content {

	/**
	 * TODO is it really necessary to separate sources and dependencies?
	 */
	SortedSet<Path> sources();

	SortedSet<Target> dependencies();

	void refresh(File destination) throws Exception;

	/**
	 * A representation that determines equality for the content
	 */
	String definitionDescription();

}
