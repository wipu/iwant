package net.sf.iwant.api.model;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

public interface IwantCoreServices {

	File compiledClasses(File dest, List<File> src, List<File> classLocations,
			boolean debug);

	int copyMissingFiles(File from, File to);

	void debugLog(String task, Object... lines);

	void downloaded(URL from, File to);

	void pipe(InputStream in, OutputStream out);

	void pipeAndClose(InputStream in, OutputStream out);

}
