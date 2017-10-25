package org.fluentjava.iwant.core;

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

	public RefreshEnvironment(File destination, RefreshEnvironment other) {
		this(destination, other.temporaryDirectory, other.locations);
	}

	public File destination() {
		return destination;
	}

	public File freshTemporaryDirectory() {
		FileUtils.ensureEmpty(temporaryDirectory.getAbsolutePath());
		return temporaryDirectory;
	}

	public Locations locations() {
		return locations;
	}

}
