package net.sf.iwant.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class ContentMock implements Content {

	private final SortedSet<Path> sources = new TreeSet();

	private final SortedSet<Target> dependencies = new TreeSet();

	private final List<File> refreshedDestinations = new ArrayList();

	private String description = "default-descr";

	public SortedSet<Target> dependencies() {
		return dependencies;
	}

	public List<File> refreshedDestinations() {
		return refreshedDestinations;
	}

	public void refresh(RefreshEnvironment refresh) throws Exception {
		refreshedDestinations.add(refresh.destination());
	}

	public SortedSet<Path> sources() {
		return sources;
	}

	public String definitionDescription() {
		return description;
	}

	public void definitionDescription(String description) {
		this.description = description;
	}

}
