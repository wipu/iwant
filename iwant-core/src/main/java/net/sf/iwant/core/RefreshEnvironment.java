package net.sf.iwant.core;

import java.io.File;

public class RefreshEnvironment {

	private final File destination;
	private final File temporaryDirectory;

	public RefreshEnvironment(File destination, File temporaryDirectory) {
		this.destination = destination;
		this.temporaryDirectory = temporaryDirectory;
	}

	public File destination() {
		return destination;
	}

	public File temporaryDirectory() {
		if (temporaryDirectory.exists()) {
			temporaryDirectory.delete();
		}
		temporaryDirectory.mkdir();
		return temporaryDirectory;
	}

}
