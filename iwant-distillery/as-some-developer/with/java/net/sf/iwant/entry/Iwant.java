package net.sf.iwant.entry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class Iwant {

	private static final boolean DEBUG_LOG = "a".contains("b");

	private final IwantNetwork network;

	private Iwant(IwantNetwork network) {
		this.network = network;
	}

	/**
	 * TODO rename, this is not only about network
	 */
	public interface IwantNetwork {

		File wantedUnmodifiable(URL url);

		URL svnkitUrl();

		URL junitUrl();

	}

	private static class RealIwantNetwork implements IwantNetwork {

		private static final File HOME = new File(
				System.getProperty("user.home"));

		public File wantedUnmodifiable(URL url) {
			return new File(HOME, "/.net.sf.iwant/wanted-unmodifiable");
		}

		public URL svnkitUrl() {
			return url("http://www.svnkit.com/"
					+ "org.tmatesoft.svn_1.3.5.standalone.nojna.zip");
		}

		public URL junitUrl() {
			final String v = "4.8.2";
			return url("http://mirrors.ibiblio.org/pub/mirrors/maven2"
					+ "/junit/junit/" + v + "/junit-" + v + ".jar");
		}

	}

	public static URL url(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static Iwant using(IwantNetwork network) {
		return new Iwant(network);
	}

	public static Iwant usingRealNetwork() {
		return new Iwant(new RealIwantNetwork());
	}

	public IwantNetwork network() {
		return network;
	}

	public static void main(String[] args) {
		try {
			usingRealNetwork().evaluate(args);
		} catch (IwantException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class IwantException extends RuntimeException {

		public IwantException(String message) {
			super(message);
		}

	}

	public void evaluate(String... args) throws Exception {
		if (args.length <= 0) {
			throw new IwantException("Usage: " + Iwant.class.getCanonicalName()
					+ " AS_SOMEONE_DIRECTORY [args...]");
		}
		File asSomeone = new File(args[0]);
		if (!asSomeone.exists()) {
			throw new IwantException("AS_SOMEONE_DIRECTORY does not exist: "
					+ asSomeone.getCanonicalPath());
		}
		File iwantWs = iwantWsrootOfWishedVersion(asSomeone);

		File iwantBootstrapClasses = iwantBootstrapperClasses(iwantWs);

		String[] iwant2Args = new String[args.length + 1];
		iwant2Args[0] = iwantWs.getCanonicalPath();
		System.arraycopy(args, 0, iwant2Args, 1, args.length);

		runJavaMain(false, true, "net.sf.iwant.entry2.Iwant2",
				new File[] { iwantBootstrapClasses }, iwant2Args);
	}

	private File iwantWsrootOfWishedVersion(File asSomeone) {
		try {
			File iHave = new File(asSomeone, "i-have");
			if (!iHave.exists()) {
				iHave.mkdir();
			}
			File iwantFrom = new File(iHave, "iwant-from");
			if (!iwantFrom.exists()) {
				new FileWriter(iwantFrom).append("iwant-from=TODO\n").close();
				throw new IwantException("I created " + iwantFrom
						+ "\nPlease edit it and rerun me.");
			}
			Properties iwantFromProps = new Properties();
			iwantFromProps.load(new FileReader(iwantFrom));
			URL iwantLocation = new URL(
					iwantFromProps.getProperty("iwant-from"));
			File iwantWs = exportedFromSvn(iwantLocation);
			return iwantWs;
		} catch (IwantException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File iwantBootstrapperClasses(File iwantWs) {
		return compiledClasses(new File(iwantWs, "bootstrap-classes"),
				iwantBootstrappingJavaSources(iwantWs),
				Collections.<File> emptyList());
	}

	public File compiledClasses(File dest, List<File> src,
			List<File> classLocations) {
		try {
			debugLog("javac", "dest: " + dest, "src:  " + src);
			ensureDir(dest);
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticListener<? super JavaFileObject> diagnosticListener = null;
			Locale locale = null;
			Charset charset = null;
			StandardJavaFileManager fileManager = compiler
					.getStandardFileManager(diagnosticListener, locale, charset);
			Iterable<? extends JavaFileObject> compilationUnits = fileManager
					.getJavaFileObjectsFromFiles(src);
			Writer compilerTaskOut = null;
			Iterable<String> classes = null;
			StringBuilder cp = new StringBuilder();
			for (Iterator<File> iterator = classLocations.iterator(); iterator
					.hasNext();) {
				File classLocation = iterator.next();
				debugLog("javac", "class-location: " + classLocation);
				cp.append(classLocation.getCanonicalPath());
				if (iterator.hasNext()) {
					cp.append(pathSeparator());
				}
			}

			List<String> options = Arrays.asList(new String[] { "-d",
					dest.getCanonicalPath(), "-classpath", cp.toString() });

			CompilationTask compilerTask = compiler.getTask(compilerTaskOut,
					fileManager, diagnosticListener, options, classes,
					compilationUnits);
			Boolean compilerTaskResult = compilerTask.call();
			fileManager.close();
			if (!compilerTaskResult) {
				throw new IwantException("Compilation failed.");
			}
			return dest;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static char pathSeparator() {
		return File.pathSeparatorChar;
	}

	private static void debugLog(String task, Object... lines) {
		if (!DEBUG_LOG) {
			return;
		}
		StringBuilder b = new StringBuilder();
		for (Object part : lines) {
			b.append(String.format("(%16s    ", task));
			b.append(part);
			b.append(")\n");
		}
		System.err.print(b);
	}

	public static void log(String task, File target) {
		StringBuilder b = new StringBuilder();
		b.append(String.format(":%16s -> ", task));
		b.append(target.getName());
		System.err.println(b);
	}

	private static List<File> iwantBootstrappingJavaSources(File iwantWs) {
		File iwant2 = new File(iwantWs,
				"iwant-distillery/src/main/java/net/sf/iwant/entry2/Iwant2.java");
		File iwant = new File(iwantWs,
				"iwant-distillery/as-some-developer/with/java/net/sf/iwant/entry/Iwant.java");
		return Arrays.asList(iwant2, iwant);
	}

	public static void runJavaMain(boolean catchPrintsAndSystemExit,
			boolean hideIwantClasses, String className, File[] classLocations,
			String... args) throws Exception {
		debugLog("invoke", "class: " + className,
				"args: " + Arrays.toString(args));
		for (File classLocation : classLocations) {
			debugLog("invoke", "class-location: " + classLocation);
		}
		ClassLoader classLoader = classLoader(hideIwantClasses, classLocations);
		Class<?> helloClass = classLoader.loadClass(className);
		Method mainMethod = helloClass.getMethod("main", String[].class);

		Object[] invocationArgs = { args };

		SecurityManager origSecman = System.getSecurityManager();
		PrintStream origOut = System.out;
		PrintStream origErr = System.err;

		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outBytes);
		boolean callSucceeded = false;
		if (catchPrintsAndSystemExit) {
			System.setSecurityManager(new ExitCatcher());
			System.setOut(out);
			System.setErr(out);
		}
		try {
			mainMethod.invoke(null, invocationArgs);
			callSucceeded = true;
		} catch (ExitCalledException e) {
			System.err.println("exit " + e.status());
			if (e.status() != 0) {
				throw new IwantException(className + " exited with "
						+ e.status());
			}
		} finally {
			// no ifs here, you never know what changes the called class did:
			System.setOut(origOut);
			System.setErr(origErr);
			System.setSecurityManager(origSecman);
			if (!callSucceeded) {
				System.err.print(outBytes.toString());
			}
		}
	}

	private static class ExitCalledException extends SecurityException {

		private final int status;

		public ExitCalledException(int status) {
			this.status = status;
		}

		public int status() {
			return status;
		}

	}

	private static class ExitCatcher extends SecurityManager {

		@Override
		public void checkPermission(Permission perm) {
			// everything allowed
		}

		@Override
		public void checkExit(int status) {
			throw new ExitCalledException(status);
		}

	}

	/**
	 * This forces the second phase of bootstrapping to use its own versions of
	 * any iwant classes so we are free to make changes to this very file
	 * without breaking things.
	 */
	private static class ClassLoaderThatHidesIwant extends ClassLoader {

		@Override
		protected synchronized Class<?> loadClass(String name, boolean resolve)
				throws ClassNotFoundException {
			if (name.startsWith("net.sf.iwant")) {
				return null;
			} else {
				return super.loadClass(name, resolve);
			}
		}

	}

	private static URLClassLoader classLoader(boolean hideIwantClasses,
			File... locations) throws MalformedURLException {
		URL[] urls = new URL[locations.length];
		for (int i = 0; i < locations.length; i++) {
			File location = locations[i];
			URL asUrl = location.toURI().toURL();
			urls[i] = asUrl;
		}
		if (hideIwantClasses) {
			return new URLClassLoader(urls, new ClassLoaderThatHidesIwant());
		} else {
			return new URLClassLoader(urls);
		}
	}

	public static String toSafeFilename(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
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

	private static void del(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				del(child);
			}
		}
		file.delete();
	}

	public File toCachePath(URL url) {
		return new File(wantedUnmodifiable(url),
				toSafeFilename(url.toExternalForm()));
	}

	public File downloaded(URL url) {
		try {
			File cached = toCachePath(url);
			if (cached.exists()) {
				return cached;
			}
			debugLog("downloaded", "from " + url);
			log("downloaded", cached);
			byte[] bytes = downloadBytes(url);
			FileOutputStream cachedOut = new FileOutputStream(cached);
			cachedOut.write(bytes);
			cachedOut.close();
			return cached;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] downloadBytes(URL url) throws MalformedURLException,
			IOException {
		InputStream in = url.openStream();
		byte[] respBody = readBytes(in);
		in.close();
		return respBody;
	}

	private static byte[] readBytes(InputStream in) throws IOException {
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		while (true) {
			int i = in.read();
			if (i < 0) {
				break;
			}
			byte b = (byte) i;
			body.write(b);
		}
		return body.toByteArray();
	}

	public File unmodifiableZipUnzipped(URL url, InputStream in) {
		try {
			File dest = new File(network.wantedUnmodifiable(url), "unzipped/"
					+ toSafeFilename(url.toExternalForm()));
			if (dest.exists()) {
				return dest;
			}
			log("unzipped", dest);
			ensureDir(dest);
			ZipInputStream zip = new ZipInputStream(in);
			ZipEntry e = null;
			byte[] buffer = new byte[32 * 1024];
			while ((e = zip.getNextEntry()) != null) {
				File entryFile = new File(dest, e.getName());
				if (e.isDirectory()) {
					ensureDir(entryFile);
					continue;
				}
				OutputStream out = new FileOutputStream(entryFile);
				while (true) {
					int bytesRead = zip.read(buffer);
					if (bytesRead <= 0) {
						break;
					}
					out.write(buffer, 0, bytesRead);
				}
				out.close();
			}
			zip.close();
			return dest;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public File unzippedSvnkit() {
		try {
			URL url = network.svnkitUrl();
			File cached = downloaded(url);
			InputStream in = new FileInputStream(cached);
			File unzipped = unmodifiableZipUnzipped(url, in);
			return unzipped;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public URL svnkitUrl() {
		return network.svnkitUrl();
	}

	private File wantedUnmodifiable(URL url) {
		File retval = network.wantedUnmodifiable(url);
		ensureDir(retval);
		return retval;
	}

	public File exportedFromSvn(URL url) {
		try {
			File exported = toCachePath(url);
			if (exported.exists()) {
				if (isFile(url)) {
					debugLog("svn-exported", "re-export needed,"
							+ " remote is a file.");
					del(exported);
				} else {
					return exported;
				}
			}
			debugLog("svn-exported", url);
			log("svn-exported", exported);
			String urlString = url.toExternalForm();
			if (isFile(url)) {
				urlString = url.getFile();
			}
			File svnkit = unzippedSvnkit();
			File svnkitJar = new File(svnkit, "svnkit-1.3.5.7406/svnkit.jar");
			File svnkitCliJar = new File(svnkit,
					"svnkit-1.3.5.7406/svnkit-cli.jar");
			runJavaMain(true, false, "org.tmatesoft.svn.cli.SVN", new File[] {
					svnkitJar, svnkitCliJar }, "export", urlString,
					exported.getCanonicalPath());
			return exported;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isFile(URL url) {
		return "file".equals(url.getProtocol());
	}

}
