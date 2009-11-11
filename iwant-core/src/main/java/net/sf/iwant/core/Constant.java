package net.sf.iwant.core;

import java.io.File;
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

	public void refresh(File destination) throws IOException {
		new FileWriter(destination).append(value()).close();
	}

	public SortedSet<Path> sources() {
		return new TreeSet();
	}

	public SortedSet<Target> dependencies() {
		return new TreeSet();
	}

	public String definitionDescription() {
		return getClass().getName() + ":" + value + "\n";
	}

}
