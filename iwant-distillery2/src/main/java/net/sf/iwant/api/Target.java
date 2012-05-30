package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface Target {

	String name();

	InputStream content(TargetEvaluationContext ctx) throws Exception;

	File path(TargetEvaluationContext ctx) throws Exception;

	/**
	 * TODO SortedSet
	 */
	List<Target> ingredients();

}
