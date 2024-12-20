package org.fluentjava.iwant.testarea;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import org.fluentjava.iwant.entry.Iwant;

public final class TestArea {

	private final File testArea;

	public static TestArea forTest(Object test) {
		return new TestArea(test.getClass());
	}

	private TestArea(Class<?> testClass) {
		File testAreaRoot = testAreaRoot();
		this.testArea = new File(testAreaRoot, testClass.getCanonicalName());
		ensureEmpty(testArea);
	}

	private File testAreaRoot() {
		try {
			File wsRootCandidate = new File(getClass()
					.getResource(getClass().getSimpleName() + ".class")
					.toURI());
			while (wsRootCandidate.getParentFile() != null) {
				wsRootCandidate = wsRootCandidate.getParentFile();
				File marker = new File(wsRootCandidate,
						"private/iwant-testarea/testarea-root");
				if (marker.exists()) {
					// earlier testarea-root was used as such, now we are saving
					// SSD and using /tmp instead, simply putting wsroot as
					// child dir for isolation:
					File testAreaRoot = new File(Iwant.IWANT_GLOBAL_TMP_DIR,
							"iwant-testarea/" + wsRootCandidate);
					return testAreaRoot;
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
		Iwant.del(file);
	}

	public static void ensureDir(File dir) {
		Iwant.mkdirs(dir);
	}

	public String contentOf(String relativePath) {
		File file = new File(root(), relativePath);
		return contentOf(file);
	}

	@SuppressWarnings("static-method")
	public String contentOf(File file) {
		try (FileReader reader = new FileReader(file)) {
			StringBuilder actual = new StringBuilder();
			int c;
			while ((c = reader.read()) >= 0) {
				actual.append((char) c);
			}
			return actual.toString();
		} catch (RuntimeException e) {
			throw e;
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

	public File hasFile(String path, String content) {
		File file = new File(root(), path);
		return fileHasContent(file, content);
	}

	@SuppressWarnings("static-method")
	public File fileHasContent(File file, String content) {
		try {
			tryToWriteTextFile(file, content);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return file;
	}

	/**
	 * Copied from Iwant.java. TODO Iwant2 must be able to put commons-io in
	 * classpath when compiling this.
	 */
	private static File tryToWriteTextFile(File file, String content)
			throws IOException {
		Iwant.mkdirs(file.getParentFile());
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.append(content);
			return file;
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public void shallContainFragmentIn(String path, String fragment) {
		String actual = contentOf(path);
		if (!actual.contains(fragment)) {
			assertEquals("File " + path + "\nshould contain:\n" + fragment,
					actual);
		}
	}

}
