package net.sf.iwant.api;

import java.io.File;
import java.io.IOException;

/**
 * TODO remove need for this class
 */
public class BackslashFixer {

	public static String wintoySafeCanonicalPath(File file) throws IOException {
		return file.getCanonicalPath().replaceAll("\\\\", "/");
	}

}
