package net.sf.iwant.api.model;

import java.io.File;
import java.net.URL;
import java.util.List;

public interface IwantCoreServices {

	File compiledClasses(File dest, List<File> src, List<File> classLocations,
			boolean debug);

	void downloaded(URL from, File to);

}
