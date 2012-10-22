package net.sf.iwant.entry3;

import java.io.File;

class DefaultUserPrefs implements UserPrefs {

	private final File userPrefsFile;

	public DefaultUserPrefs(File userPrefsFile) {
		this.userPrefsFile = userPrefsFile;
	}

	@Override
	public String toString() {
		return "default user preferences (file " + userPrefsFile
				+ " is missing):\n[workerCount=" + workerCount() + "]";
	}

	@Override
	public int workerCount() {
		// some parallelism, but not too much
		return 2;
	}

}
