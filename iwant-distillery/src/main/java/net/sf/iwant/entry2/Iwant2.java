package net.sf.iwant.entry2;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;

public class Iwant2 {

	private final IwantNetwork network;
	private final Iwant iwant;

	public Iwant2(IwantNetwork network) {
		this.network = network;
		this.iwant = Iwant.using(network);
	}

	public static void main(String[] args) {
		File iwantWs = new File(args[0]);
		String[] args2 = new String[args.length - 1];
		System.arraycopy(args, 1, args2, 0, args2.length);
		try {
			using(Iwant.usingRealNetwork().network()).evaluate(iwantWs, args2);
		} catch (IwantException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public static Iwant2 using(IwantNetwork network) {
		return new Iwant2(network);
	}

	public void evaluate(File iwantWs, String... args) {
		try {
			File junitJar = junitJar();

			File allIwantClasses = iwant.toCachePath(new File(iwantWs,
					"all-classes").toURI().toURL());
			Iwant.ensureDir(allIwantClasses);

			List<File> src = new ArrayList<File>();
			src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery/"
					+ "as-some-developer/with/java/" + "net/sf/iwant/entry"));
			src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery/"
					+ "src/main/java/" + "net/sf/iwant/entry2"));
			src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery/"
					+ "src/test/java/" + "net/sf/iwant/entry"));
			src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
					+ "src/main/java/" + "net/sf/iwant/entry3"));
			src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-distillery2/"
					+ "src/test/java/" + "net/sf/iwant/entry3"));
			src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-testarea/"
					+ "src/main/java/" + "net/sf/iwant/testarea"));
			src.addAll(srcFilesOfPackageDir(iwantWs, "iwant-testrunner/"
					+ "src/main/java/" + "net/sf/iwant/testrunner"));
			iwant.compiledClasses(allIwantClasses, src, Arrays.asList(junitJar));

			File testArea = new File(iwantWs,
					"iwant-testarea/testarea-classdir");
			File classpathMarker = new File(iwantWs,
					"iwant-distillery/classpath-marker");

			File[] classLocations = { classpathMarker, testArea,
					allIwantClasses, junitJar };

			Iwant.runJavaMain(true, true, false,
					"net.sf.iwant.testrunner.IwantTestRunner", classLocations,
					"net.sf.iwant.entry3.IwantEntry3Suite");

			Iwant.runJavaMain(false, false, false,
					"net.sf.iwant.entry3.Iwant3", classLocations, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static List<File> srcFilesOfPackageDir(File iwantWs,
			String packagePath) {
		List<File> srcFiles = new ArrayList<File>();
		File packageDir = new File(iwantWs, packagePath);
		File[] files = packageDir.listFiles();
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
