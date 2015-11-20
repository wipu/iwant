package net.sf.iwant.entry3;

import java.io.File;
import java.util.Properties;

import net.sf.iwant.entry.Iwant;

public class UserPrefsImpl implements UserPrefs {

	private final int workerCount;
	private final File file;

	public UserPrefsImpl(Properties props, File file) {
		this.file = file;
		String workerCountString = props.getProperty("workerCount");
		if (workerCountString == null) {
			throw new Iwant.IwantException(
					"Please specify workerCount in " + file);
		}
		try {
			workerCount = Integer.parseInt(workerCountString);
		} catch (NumberFormatException e) {
			throw new Iwant.IwantException(
					"Please specify workerCount as an integer in " + file);
		}
		if (workerCount <= 0) {
			throw new Iwant.IwantException(
					"Please specify workerCount as a positive integer in "
							+ file);
		}
	}

	@Override
	public int workerCount() {
		return workerCount;
	}

	@Override
	public String toString() {
		return "user preferences from file " + file + ":\n[workerCount="
				+ workerCount() + "]";
	}

}
