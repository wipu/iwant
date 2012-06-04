package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface Path {

	String name();

	InputStream content(TargetEvaluationContext ctx) throws Exception;

	File cachedAt(CacheLocations cached);

	void path(TargetEvaluationContext ctx) throws Exception;

	/**
	 * TODO SortedSet
	 */
	List<Path> ingredients();

	String contentDescriptor();

}
