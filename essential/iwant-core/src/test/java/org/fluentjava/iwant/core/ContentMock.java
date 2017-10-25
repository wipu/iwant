package net.sf.iwant.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class ContentMock implements Content {

	private final SortedSet<Path> ingredients = new TreeSet<Path>();

	private final List<File> refreshedDestinations = new ArrayList<File>();

	private String description = "default-descr";

	@Override
	public SortedSet<Path> ingredients() {
		return ingredients;
	}

	public List<File> refreshedDestinations() {
		return refreshedDestinations;
	}

	@Override
	public void refresh(RefreshEnvironment refresh) throws Exception {
		refreshedDestinations.add(refresh.destination());
	}

	@Override
	public String definitionDescription() {
		return description;
	}

	public void definitionDescription(String description) {
		this.description = description;
	}

}
