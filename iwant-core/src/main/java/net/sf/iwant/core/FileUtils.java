package net.sf.iwant.core;

import java.io.File;
import java.io.IOException;

public class FileUtils {

	/**
	 * TODO create and reuse a fluent reusable file declaration library
	 */
	public static void ensureDir(File dir) {
		File parent = dir.getParentFile();
		if (!parent.exists()) {
			ensureDir(parent);
		}
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	/**
	 * TODO encapsulate all information inside a Path abstraction so we can get
	 * an absolute or a relative path whenever needed.
	 */
	public static String abs(String path) {
		try {
			return new File(path).getCanonicalPath();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void ensureParentDirFor(String fileName) {
		File file = new File(fileName);
		File parent = file.getParentFile();
		ensureDir(parent);
	}

	public static void ensureEmpty(String dirname) {
		File dir = new File(dirname);
		del(dir);
		ensureDir(dir);
	}

	public static void del(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				del(child);
			}
		}
		file.delete();
	}

}
