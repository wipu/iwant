package net.sf.iwant.core;

import java.io.File;

public class RefreshEnvironment {

	private final File destination;
	private final File temporaryDirectory;
	private final Locations locations;

	public RefreshEnvironment(File destination, File temporaryDirectory,
			Locations locations) {
		this.destination = destination;
		this.temporaryDirectory = temporaryDirectory;
		this.locations = locations;
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

	public Locations locations() {
		return locations;
	}

}
