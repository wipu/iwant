package org.fluentjava.iwant.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

class TestArea {

	private final String testArea;
	private final String wsRoot;
	private final String cacheDir;
	private final String mockWeb;
	private final String iwantLibs;

	static TestArea newEmpty() {
		return new TestArea();
	}

	private TestArea() {
		testArea = new File(getClass().getResource("/iwanttestarea").getPath())
				.getAbsolutePath();
		wsRoot = testArea + "/wsroot";
		ensureEmpty(wsRoot);
		cacheDir = testArea + "/iwant-cached-test";
		ensureEmpty(cacheDir);
		mockWeb = testArea + "/mock-web";
		ensureEmpty(mockWeb);

		iwantLibs = testArea
				+ "/.internal/iwant/iwant-bootstrapper/phase2/iw/cached/.internal/bin";
		String eclipseClasses = testArea + "/../../classes";
		if (new File(eclipseClasses).exists()) {
			ensureEmpty(iwantLibs + "/iwant-core");
			copy(eclipseClasses, iwantLibs + "/iwant-core");
			copy(testArea + "/../../../iwant-lib-ant-1.7.1/ant-1.7.1.jar",
					iwantLibs);
			copy(testArea + "/../../../iwant-lib-ant-1.7.1/ant-junit-1.7.1.jar",
					iwantLibs);
			copy(testArea + "/../../../iwant-lib-junit-3.8.1/junit-3.8.1.jar",
					iwantLibs);
		}
	}

	Locations asLocations() {
		return new Locations(wsRoot, wsRoot + "/as-test-developer", cacheDir,
				iwantLibs);
	}

	String testArea() {
		return testArea;
	}

	String wsRoot() {
		return wsRoot;
	}

	String cacheDir() {
		return cacheDir;
	}

	String mockWeb() {
		return mockWeb;
	}

	String iwantLibs() {
		return iwantLibs;
	}

	private static void ensureEmpty(String dirname) {
		FileUtils.ensureEmpty(dirname);
	}

	private static void copy(String from, String to) {
		Copy copy = new Copy();
		File fromFile = new File(from);
		if (fromFile.isDirectory()) {
			Project project = new Project();
			copy.setProject(project);
			FileSet dirSet = new FileSet();
			dirSet.setDir(fromFile);
			dirSet.createPatternSet().setIncludes("**");
			copy.add(dirSet);
		} else {
			copy.setFile(fromFile);
		}
		copy.setTodir(new File(to));
		executeSilently(copy);
	}

	private static void executeSilently(Task task) {
		PrintStream oldOut = System.out;
		PrintStream oldErr = System.err;
		ByteArrayOutputStream newOut = new ByteArrayOutputStream();
		ByteArrayOutputStream newErr = new ByteArrayOutputStream();
		System.setOut(new PrintStream(newOut));
		System.setErr(new PrintStream(newErr));
		try {
			task.execute();
		} finally {
			System.setOut(oldOut);
			System.setErr(oldErr);
		}
	}

	protected String cachedContent(String target) throws IOException {
		return contentOf(pathToCachedTarget(target));
	}

	protected static String contentOf(String path) throws IOException {
		FileReader reader = new FileReader(path);
		StringBuilder actual = new StringBuilder();
		int c;
		while ((c = reader.read()) >= 0) {
			actual.append((char) c);
		}
		reader.close();
		return actual.toString();
	}

	protected String pathToCachedTarget(String target) {
		return cacheDir() + "/target/" + target;
	}

}
