package org.fluentjava.iwant.core;

import java.util.SortedSet;

public interface Content {

	SortedSet<Path> ingredients();

	void refresh(RefreshEnvironment refresh) throws Exception;

	/**
	 * A representation that determines equality for the content
	 */
	String definitionDescription();

}
