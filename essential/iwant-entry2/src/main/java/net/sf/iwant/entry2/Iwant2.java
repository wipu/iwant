package net.sf.iwant.entry2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

	public static class ClassesFromUnmodifiableIwantEssential extends
			UnmodifiableSource<File> {

		public ClassesFromUnmodifiableIwantEssential(File location) {
			super(location);
		}

	}

	public static void main(String[] args) throws Exception {
		Iwant.fileLog("Iwant2: " + Arrays.toString(args));
		File iwantEssential = new File(args[0]);
		String[] args2 = new String[args.length - 1];
		System.arraycopy(args, 1, args2, 0, args2.length);
		try {
			using(Iwant.usingRealNetwork().network()).evaluate(iwantEssential,
					args2);
		} catch (IwantException e) {
			Iwant.fileLog("Iwant2 main failed: " + e);
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public static Iwant2 using(IwantNetwork network) {
		return new Iwant2(network);
	}

	public void evaluate(File iwantEssential, String... args) throws Exception {
		Iwant.debugLog("evaluate", (Object[]) args);
		File allIwantClasses = allIwantClasses(iwantEssential);

		File wsRootMarker = new File(iwantEssential, "iwant-wsroot-marker");

		List<File> classLocations = Arrays
				.asList(wsRootMarker, allIwantClasses);

		String[] iwant3Args = new String[args.length + 1];
		iwant3Args[0] = iwantEssential.getCanonicalPath();
		System.arraycopy(args, 0, iwant3Args, 1, args.length);
		Iwant.runJavaMain(false, false, "net.sf.iwant.entry3.Iwant3",
				classLocations, iwant3Args);
	}

	private static List<String> relativeIwantSrcDirs() {
		List<String> srcDirs = new ArrayList<String>();
		srcDirs.add("iwant-api-core/" + "src/main/java");
		srcDirs.add("iwant-api-javamodules/" + "src/main/java");
		srcDirs.add("iwant-api-model/" + "src/main/java");
		srcDirs.add("iwant-api-wsdef/" + "src/main/java");
		srcDirs.add("iwant-core-ant/" + "src/main/java");
		srcDirs.add("iwant-core-download/" + "src/main/java");
		srcDirs.add("iwant-coreservices/" + "src/main/java");
		srcDirs.add("iwant-deprecated-emma/" + "src/main/java");
		srcDirs.add("iwant-entry/" + "as-some-developer/with/java");
		srcDirs.add("iwant-eclipse-settings/" + "src/main/java");
		srcDirs.add("iwant-entry2/" + "src/main/java");
		srcDirs.add("iwant-entry3/" + "src/main/java");
		srcDirs.add("iwant-iwant-wsroot-finder/" + "src/main/java");
		srcDirs.add("iwant-planner/" + "src/main/java");
		srcDirs.add("iwant-planner-api/" + "src/main/java");
		return srcDirs;
	}

	public static SortedSet<File> srcDirsOfIwantWs(File iwantEssential) {
		SortedSet<File> srcDirs = new TreeSet<File>();
		for (String relative : relativeIwantSrcDirs()) {
			srcDirs.add(new File(iwantEssential, relative));
		}
		return srcDirs;
	}

	public File allIwantClasses(File iwantEssential) {
		Iwant.fileLog("allIwantClasses, iwantEssential=" + iwantEssential);
		File allIwantClasses = network
				.cacheLocation(new ClassesFromUnmodifiableIwantEssential(
						iwantEssential));
		Iwant.fileLog("allIwantClasses, dest=" + allIwantClasses);

		SortedSet<File> javaFiles = new TreeSet<File>();
		for (File srcDir : srcDirsOfIwantWs(iwantEssential)) {
			javaFiles.addAll(javaFilesRecursivelyUnder(srcDir));
		}

		TimestampHandler timestampHandler = new TimestampHandler(
				allIwantClasses, new File(allIwantClasses.getAbsolutePath()
						+ ".srcdescr"), javaFiles);
		if (!timestampHandler.needsRefresh()) {
			Iwant.fileLog("allIwantClasses does not need refresh.");
			return allIwantClasses;
		}

		Iwant.log("compiled", allIwantClasses);
		if (allIwantClasses.exists()) {
			Iwant.del(allIwantClasses);
		}
		allIwantClasses.mkdirs();

		List<File> javaFileList = new ArrayList<File>(javaFiles);
		iwant.compiledClasses(allIwantClasses, javaFileList,
				Collections.<File> emptyList(), true, null);
		timestampHandler.markFresh();
		return allIwantClasses;
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