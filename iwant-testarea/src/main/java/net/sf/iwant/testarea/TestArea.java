package net.sf.iwant.testarea;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public abstract class TestArea {

	private final File testArea;

	public TestArea() {
		File testAreas = new File(getClass().getResource("/iwant-testareas")
				.getPath());
		this.testArea = new File(testAreas, getClass().getCanonicalName());
		ensureEmpty(testArea);
	}

	public File root() {
		return testArea;
	}

	public File newDir(String dirName) {
		File dir = new File(root(), dirName);
		ensureDir(dir);
		return dir;
	}

	private static void ensureEmpty(File dir) {
		del(dir);
		ensureDir(dir);
	}

	private static void del(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				del(child);
			}
		}
		file.delete();
	}

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

	public String contentOf(String relativePath) throws IOException {
		File file = new File(root(), relativePath);
		FileReader reader = new FileReader(file);
		StringBuilder actual = new StringBuilder();
		int c;
		while ((c = reader.read()) >= 0) {
			actual.append((char) c);
		}
		reader.close();
		return actual.toString();
	}

}
