package net.sf.iwant.entry2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.Iwant.UnmodifiableSource;

public class Iwant2 {

	private final IwantNetwork network;
	private final Iwant iwant;

	public Iwant2(IwantNetwork network) {
		this.network = network;
		this.iwant = Iwant.using(network);
	}

	public static class ClassesFromUnmodifiableIwantWsRoot extends
			UnmodifiableSource<File> {

		public ClassesFromUnmodifiableIwantWsRoot(File location) {
			super(location);
		}

	}

	public static void main(String[] args) throws Exception {
		Iwant.fileLog("Iwant2: " + Arrays.toString(args));
		File iwantWs = new File(args[0]);
		String[] args2 = new String[args.length - 1];
		System.arraycopy(args, 1, args2, 0, args2.length);
		try {
			using(Iwant.usingRealNetwork().network()).evaluate(iwantWs, args2);
		} catch (IwantException e) {
			Iwant.fileLog("Iwant2 main failed: " + e);
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public static Iwant2 using(IwantNetwork network) {
		return new Iwant2(network);
	}

	public void evaluate(File iwantWs, String... args) throws Exception {
		Iwant.debugLog("evaluate", (Object[]) args);
		File allIwantClasses = allIwantClasses(iwantWs);

		File testArea = new File(iwantWs, "iwant-testarea/testarea-classdir");
		File classpathMarker = new File(iwantWs,
				"iwant-distillery/classpath-marker");

		List<File> classLocations = Arrays.asList(classpathMarker, testArea,
				allIwantClasses, junitJar());

		// no file results from test run:
		File cachedTestResult = null;
		TimestampHandler timestampHandler = new TimestampHandler(
				cachedTestResult, new File(allIwantClasses.getAbsolutePath()
						+ ".srcdescr-of-testrun"),
				plainFilesRecursivelyUnder(Collections
						.singleton(allIwantClasses)));
		if (timestampHandler.needsRefresh()) {
			Iwant.log("self-tested", allIwantClasses);
			ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
			PrintStream err = new PrintStream(errBytes);
			PrintStream origErr = System.err;
			System.setErr(err);
			boolean testsPassed = false;
			try {
				Iwant.runJavaMain(true, false,
						"net.sf.iwant.testrunner.IwantTestRunner",
						classLocations, "net.sf.iwant.IwantDistillery2Suite");
				testsPassed = true;
			} finally {
				System.setErr(origErr);
				if (!testsPassed) {
					err.close();
					System.err
							.print("Tests failed => displaying combined out and err:\n"
									+ errBytes.toString());
				}
			}
			timestampHandler.markFresh();
		}

		Iwant.runJavaMain(false, false, "net.sf.iwant.entry3.Iwant3",
				classLocations, args);
	}

	public File allIwantClasses(File iwantWs) {
		Iwant.fileLog("allIwantClasses, iwantWs=" + iwantWs);
		File allIwantClasses = network
				.cacheLocation(new ClassesFromUnmodifiableIwantWsRoot(iwantWs));
		Iwant.fileLog("allIwantClasses, dest=" + allIwantClasses);

		SortedSet<File> src = new TreeSet<File>();
		src.addAll(javaFilesOfSrcDir(iwantWs, "iwant-distillery/"
				+ "as-some-developer/with/java"));
		src.addAll(javaFilesOfSrcDir(iwantWs, "iwant-distillery/"
				+ "src/main/java"));
		src.addAll(javaFilesOfSrcDir(iwantWs, "iwant-distillery/"
				+ "src/test/java"));
		src.addAll(javaFilesOfSrcDir(iwantWs, "iwant-distillery2/"
				+ "src/test/java"));
		src.addAll(javaFilesOfSrcDir(iwantWs, "iwant-distillery2/"
				+ "src/main/java"));
		src.addAll(javaFilesOfSrcDir(iwantWs, "iwant-testarea/"
				+ "src/main/java"));
		src.addAll(javaFilesOfSrcDir(iwantWs, "iwant-testrunner/"
				+ "src/main/java"));

		TimestampHandler timestampHandler = new TimestampHandler(
				allIwantClasses, new File(allIwantClasses.getAbsolutePath()
						+ ".srcdescr"), src);
		if (!timestampHandler.needsRefresh()) {
			Iwant.fileLog("allIwantClasses does not need refresh.");
			return allIwantClasses;
		}

		Iwant.log("compiled", allIwantClasses);
		if (allIwantClasses.exists()) {
			Iwant.del(allIwantClasses);
		}
		allIwantClasses.mkdirs();

		List<File> srcList = new ArrayList<File>(src);
		iwant.compiledClasses(allIwantClasses, srcList,
				Arrays.asList(junitJar()), true);
		timestampHandler.markFresh();
		return allIwantClasses;
	}

	private static SortedSet<File> javaFilesOfSrcDir(File iwantWs,
			String srcDirPath) {
		File srcDir = new File(iwantWs, srcDirPath);
		return javaFilesRecursivelyUnder(srcDir);
	}

	public static SortedSet<File> javaFilesRecursivelyUnder(File dir) {
		SortedSet<File> srcFiles = new TreeSet<File>();
		File[] files = dir.listFiles();
		for (File file : files) {
			if (".svn".equals(file.getName())) {
				continue;
			}
			if (file.isDirectory()) {
				srcFiles.addAll(javaFilesRecursivelyUnder(file));
			} else if (isJavaSourceFile(file)) {
				srcFiles.add(file);
			}
		}
		return srcFiles;
	}

	private static boolean isJavaSourceFile(File file) {
		return !file.isDirectory() && file.getAbsolutePath().endsWith(".java");
	}

	private File junitJar() {
		try {
			URL url = network.junitUrl();
			return iwant.downloaded(url);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	static class TimestampHandler {

		private final File cachedTarget;
		private final File sourceDescriptor;
		private final SortedSet<File> sources;

		public TimestampHandler(File cachedTarget, File sourceDescriptor,
				SortedSet<File> sources) {
			this.cachedTarget = cachedTarget;
			this.sourceDescriptor = sourceDescriptor;
			this.sources = sources;
		}

		void markFresh() {
			Iwant.fileLog("Writing " + sourceDescriptor);
			Iwant.newTextFile(sourceDescriptor,
					currentSourceDescriptorContent());
		}

		boolean needsRefresh() {
			boolean needsRefresh = checkIfNeedsToRefresh();
			if (needsRefresh) {
				Iwant.fileLog("Deleting " + sourceDescriptor);
				sourceDescriptor.delete();
			}
			return needsRefresh;
		}

		private boolean checkIfNeedsToRefresh() {
			if (cachedTarget != null && !cachedTarget.exists()) {
				return true;
			}
			if (!sourceDescriptor.exists()) {
				return true;
			}
			long sourceDescriptorTimestamp = sourceDescriptor.lastModified();
			for (File source : sources) {
				if (source.lastModified() >= sourceDescriptorTimestamp) {
					return true;
				}
			}
			String lastSources = contentAsString(sourceDescriptor);
			String currentSources = currentSourceDescriptorContent();
			if (!currentSources.equals(lastSources)) {
				return true;
			}
			return false;
		}

		/**
		 * TODO canonical?
		 */
		private String currentSourceDescriptorContent() {
			StringBuilder b = new StringBuilder();
			for (File source : sources) {
				b.append(source.getAbsolutePath()).append("\n");
			}
			return b.toString();
		}

	}

	public static String contentAsString(File file) {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return toString(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static String toString(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pipe(in, out);
		return out.toString();
	}

	/**
	 * TODO use nio transfer?
	 */
	public static void pipe(InputStream in, OutputStream out) {
		try {
			byte[] buf = new byte[8192];
			while (true) {
				int bytesRead = in.read(buf);
				if (bytesRead < 0) {
					return;
				}
				out.write(buf, 0, bytesRead);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static SortedSet<File> plainFilesRecursivelyUnder(Collection<File> in) {
		SortedSet<File> retval = new TreeSet<File>();
		for (File f : in) {
			plainFilesRecursivelyUnder(retval, f);
		}
		return retval;
	}

	private static void plainFilesRecursivelyUnder(Collection<File> retval,
			File f) {
		if (!f.isDirectory()) {
			retval.add(f);
			return;
		}
		for (File child : f.listFiles()) {
			plainFilesRecursivelyUnder(retval, child);
		}
	}

	static SortedSet<File> filesByNameSuffix(Collection<File> in, String suffix) {
		SortedSet<File> out = new TreeSet<File>();
		for (File candidate : in) {
			if (candidate.getName().endsWith(suffix)) {
				out.add(candidate);
			}
		}
		return out;
	}

}
