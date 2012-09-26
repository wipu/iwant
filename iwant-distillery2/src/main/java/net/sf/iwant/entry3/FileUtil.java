package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;

/**
 * TODO remove need for this class
 */
public class FileUtil {

	public static String relativePathOfFileUnderParent(File child, File parent)
			throws IOException {
		String parentPath = parent.getCanonicalPath();
		String childPath = child.getCanonicalPath();
		String replaced = childPath.replaceFirst("^" + parentPath, "");
		if (replaced.equals(childPath)) {
			throw new IllegalArgumentException(child + " is not a child of "
					+ parent);
		}
		return replaced.replaceFirst("^/", "");
	}

}
