package net.sf.iwant.testarea;

import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;

public abstract class TestArea {

	private final File testArea;

	public TestArea() {
		File testAreaRoot = testAreaRoot();
		this.testArea = new File(testAreaRoot, getClass().getCanonicalName());
		ensureEmpty(testArea);
	}

	private File testAreaRoot() {
		try {
			File wsRootCandidate = new File(getClass().getResource(
					getClass().getSimpleName() + ".class").toURI());
			while (wsRootCandidate.getParentFile() != null) {
				wsRootCandidate = wsRootCandidate.getParentFile();
				File testAreaRootCandidate = new File(wsRootCandidate,
						"iwant-testarea/testarea-root");
				if (testAreaRootCandidate.exists()) {
					return testAreaRootCandidate;
				}
			}
			throw new IllegalStateException("Cannot find testarea root");
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	public File root() {
		return testArea;
	}

	public File newDir(String dirName) {
		File dir = new File(root(), dirName);
		ensureDir(dir);
		return dir;
	}

	public static void ensureEmpty(File dir) {
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

	public String contentOf(String relativePath) {
		File file = new File(root(), relativePath);
		return contentOf(file);
	}

	public String contentOf(File file) {
		try {
			FileReader reader = new FileReader(file);
			StringBuilder actual = new StringBuilder();
			int c;
			while ((c = reader.read()) >= 0) {
				actual.append((char) c);
			}
			reader.close();
			return actual.toString();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static boolean bytesContain(byte[] bytes, String stringToFind) {
		for (int i = 0; i < bytes.length - stringToFind.length(); i++) {
			if (bytesHasAt(bytes, i, stringToFind)) {
				return true;
			}
		}
		return false;
	}

	private static boolean bytesHasAt(byte[] bytes, int location,
			String stringToFind) {
		for (int i = 0; i < stringToFind.length(); i++) {
			byte expected = (byte) stringToFind.charAt(i);
			if (bytes[location + i] != expected) {
				return false;
			}
		}
		return true;
	}

}
