package net.sf.iwant.core;

import java.io.File;

class TimestampReaderFileImpl implements TimestampReader {

	private final Locations locations;

	TimestampReaderFileImpl(Locations locations) {
		this.locations = locations;
	}

	public Long modificationTime(Path path) {
		File file = new File(path.asAbsolutePath(locations));
		if (!file.exists()) {
			TextOutput.debugLog("File does not exist: " + file);
			return null;
		}
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
