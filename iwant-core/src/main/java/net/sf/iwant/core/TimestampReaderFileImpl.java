package net.sf.iwant.core;

import java.io.File;

class TimestampReaderFileImpl implements TimestampReader {

	public Long modificationTime(Path path) {
		File file = new File(path.name());
		if (!file.exists())
			return null;
		return modificationTime(file);
	}

	/**
	 * TODO let ant handle this, it can ignore svn directories etc
	 */
	private Long modificationTime(File file) {
		long time = file.lastModified();
		if (file.isDirectory()) {
			// a directory is always treated as a whole
			for (File child : file.listFiles()) {
				time = Math.max(time, modificationTime(child));
			}
		}
		return time;
	}

}
