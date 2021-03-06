package org.fluentjava.iwant.core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

public class Constant implements Content {

	private final String value;

	public Constant(String value) {
		this.value = value;
	}

	public static Constant value(String value) {
		return new Constant(value);
	}

	public String value() {
		return value;
	}

	@Override
	public void refresh(RefreshEnvironment refresh) throws IOException {
		new FileWriter(refresh.destination()).append(value()).close();
	}

	@Override
	public SortedSet<Path> ingredients() {
		return new TreeSet<Path>();
	}

	@Override
	public String definitionDescription() {
		return getClass().getName() + ":" + value + "\n";
	}

}
