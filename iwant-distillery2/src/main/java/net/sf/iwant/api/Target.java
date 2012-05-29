package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;

public interface Target {

	String name();

	InputStream content();

	void refreshTo(File cachedContent) throws Exception;

}
