package net.sf.iwant.entry2;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

		File[] classLocations = { classpathMarker, testArea, allIwantClasses,
				junitJar() };

		Iwant.log("self-tested", allIwantClasses);
		Iwant.runJavaMain(true, false,
				"net.sf.iwant.testrunner.IwantTestRunner", classLocations,
				"net.sf.iwant.IwantDistillery2Suite");

		Iwant.runJavaMain(false, false, "net.sf.iwant.entry3.Iwant3",
				classLocations, args);
	}

	public File allIwantClasses(File iwantWs) {
		Iwant.fileLog("allIwantClasses, iwantWs=" + iwantWs);
		File allIwantClasses = network
				.cacheLocation(new ClassesFromUnmodifiableIwantWsRoot(iwantWs));
		Iwant.fileLog("allIwantClasses, dest=" + allIwantClasses);
		// TODO need for laziness?
		if (allIwantClasses.exists()) {
			System.err.println("TODO laziness, and for testing, too");
			Iwant.del(allIwantClasses);
		}
		Iwant.ensureDir(allIwantClasses);

		List<File> src = new ArrayList<File>();
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery/"
				+ "as-some-developer/with/java/" + "net/sf/iwant/entry"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery/"
				+ "src/main/java/" + "net/sf/iwant/entry2"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery/"
				+ "src/test/java/" + "net/sf/iwant/entry"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/test/java/" + "net/sf/iwant"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/test/java/" + "net/sf/iwant/api"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/main/java/" + "net/sf/iwant/api"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/test/java/" + "net/sf/iwant/eclipsesettings"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/main/java/" + "net/sf/iwant/eclipsesettings"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/main/java/" + "net/sf/iwant/entry3"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/test/java/" + "net/sf/iwant/entry3"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/test/java/" + "net/sf/iwant/io"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/main/java/" + "net/sf/iwant/io"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/test/java/" + "net/sf/iwant/planner"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
				+ "src/main/java/" + "net/sf/iwant/planner"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-testarea/"
				+ "src/main/java/" + "net/sf/iwant/testarea"));
		src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-testrunner/"
				+ "src/main/java/" + "net/sf/iwant/testrunner"));
		iwant.compiledClasses(allIwantClasses, src, Arrays.asList(junitJar()));
		return allIwantClasses;
	}

	private static List<File> srcFilesOfPackageDir(File iwantWs,
			String packagePath) {
		File packageDir = new File(iwantWs, packagePath);
		return javaFilesUnder(packageDir);
	}

	public static List<File> javaFilesUnder(File dir) {
		List<File> srcFiles = new ArrayList<File>();
		File[] files = dir.listFiles();
		Arrays.sort(files);
		for (File file : files) {
			if (isJavaSourceFile(file)) {
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

}
