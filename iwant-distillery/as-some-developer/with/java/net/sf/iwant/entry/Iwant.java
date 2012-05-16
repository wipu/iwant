package net.sf.iwant.entry;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.Date;
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

		File cacheLocation(UnmodifiableSource<?> src);

		URL svnkitUrl();

		URL junitUrl();

	}

	public static abstract class UnmodifiableSource<T> {

		private final T location;

		public UnmodifiableSource(T location) {
			this.location = location;
		}

		public String rawLocationString() {
			return location.toString();
		}

		@Override
		public final String toString() {
			return getClass().getSimpleName() + ":" + rawLocationString();
		}

		public final T location() {
			return location;
		}

		@Override
		public boolean equals(Object obj) {
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			UnmodifiableSource<?> other = (UnmodifiableSource<?>) obj;
			return location.equals(other.location());
		}

		@Override
		public int hashCode() {
			return location.hashCode();
		}

	}

	public static class UnmodifiableUrl extends UnmodifiableSource<URL> {

		public UnmodifiableUrl(URL location) {
			super(location);
		}

	}

	public static class UnmodifiableZip extends UnmodifiableSource<URL> {

		public UnmodifiableZip(URL location) {
			super(location);
		}

	}

	public static class UnmodifiableIwantBootstrapperClassesFromIwantWsRoot
			extends UnmodifiableSource<URL> {

		public UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(File iwantWs) {
			super(fileToUrl(iwantWs));
		}

	}

	public static URL fileToUrl(File file) {
		try {
			URL url = file.toURI().toURL();
			String urlString = url.toExternalForm();
			url = new URL(withoutTrailingSlash(urlString));
			return url;
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static URL withTrailingSlashIfDir(URL url) {
		try {
			// here we trust this is only used for file urls
			File asFile = new File(url.toURI());
			if (!asFile.isDirectory()) {
				return url;
			}
			String urlString = url.toExternalForm();
			if (urlString.endsWith("/")) {
				return url;
			}
			return new URL(urlString + "/");
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static class RealIwantNetwork implements IwantNetwork {

		private static final File HOME = new File(
				System.getProperty("user.home"));

		@Override
		public File cacheLocation(UnmodifiableSource<?> src) {
			File cached = new File(HOME, ".net.sf.iwant/cached");
			File cachedFromSrc = new File(cached, src.getClass()
					.getSimpleName());
			String fileName = toSafeFilename(src.rawLocationString());
			return new File(cachedFromSrc, fileName);
		}

		@Override
		public URL svnkitUrl() {
			return url("http://www.svnkit.com/"
					+ "org.tmatesoft.svn_1.3.5.standalone.nojna.zip");
		}

		@Override
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

	public static void main(String[] args) throws Exception {
		try {
			usingRealNetwork().evaluate(args);
		} catch (IwantException e) {
			System.err.println(e.getMessage());
			System.exit(1);
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
		URL iwantLocation = iwantFrom(asSomeone);
		File iwantWs = exportedFromSvn(iwantLocation);
		return iwantWs;
	}

	private static URL iwantFrom(File asSomeone) {
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
			return iwantLocation;
		} catch (IwantException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File iwantBootstrapperClasses(File iwantWs) {
		File classes = network
				.cacheLocation(new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
						iwantWs));
		return compiledClasses(classes, iwantBootstrappingJavaSources(iwantWs),
				Collections.<File> emptyList());
	}

	public File compiledClasses(File dest, List<File> src,
			List<File> classLocations) {
		try {
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
			return compiledClasses(dest, src, cp.toString());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public File compiledClasses(File dest, List<File> src, String classpath) {
		try {
			fileLog("compiledClasses " + dest);
			debugLog("javac", "dest: " + dest);
			for (File srcFile : src) {
				debugLog("javac", "src: " + srcFile);
			}
			del(dest);
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

			List<String> options = Arrays.asList(new String[] { "-d",
					dest.getCanonicalPath(), "-classpath", classpath });

			CompilationTask compilerTask = compiler.getTask(compilerTaskOut,
					fileManager, diagnosticListener, options, classes,
					compilationUnits);
			Boolean compilerTaskResult = compilerTask.call();
			fileManager.close();
			if (!compilerTaskResult) {
				throw new IwantException("Compilation failed.");
			}
			new FileWriter(new File(dest, "compiled-by-Iwant")).append("true")
					.close();
			return dest;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static char pathSeparator() {
		return File.pathSeparatorChar;
	}

	public static void debugLog(String task, Object... lines) {
		StringBuilder b = new StringBuilder();
		for (Object part : lines) {
			b.append(String.format("(%16s    ", task));
			b.append(part);
			b.append(")\n");
		}
		fileLog(b.toString());
		if (DEBUG_LOG) {
			System.err.print(b);
		}
	}

	public static void log(String task, File target) {
		StringBuilder b = new StringBuilder();
		b.append(String.format(":%16s -> ", task));
		b.append(target.getName());
		System.err.println(b);
		fileLog(b.toString());
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
		debugLog("invoke", "catchPrintsAndSystemExit="
				+ catchPrintsAndSystemExit);
		debugLog("invoke", "hideIwantClasses=" + hideIwantClasses);
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
			if (catchPrintsAndSystemExit) {
				out.close();
				fileLog("caught out/err: " + outBytes.toString());
			}
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

	public static ClassLoader classLoader(boolean hideIwantClasses,
			File[] locations) {
		ClassLoader parent = hideIwantClasses ? new ClassLoaderThatHidesIwant()
				: null;
		return classLoader(parent, locations);
	}

	public static ClassLoader classLoader(ClassLoader parent, File[] locations) {
		URL[] urls = new URL[locations.length];
		for (int i = 0; i < locations.length; i++) {
			File location = locations[i];
			URL asUrl = fileToUrl(location);
			// TODO own type so we don't need to slash back and forth
			asUrl = withTrailingSlashIfDir(asUrl);
			urls[i] = asUrl;
		}
		if (parent != null) {
			return new URLClassLoader(urls, parent);
		} else {
			return new URLClassLoader(urls);
		}
	}

	public static void fileLog(String msg) {
		try {
			new FileWriter("/tmp/iwant-cl", true).append(new Date().toString())
					.append(" - ").append(msg).append("\n").close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
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

	public static void del(File file) {
		debugLog("del " + file);
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				del(child);
			}
		}
		file.delete();
	}

	public File downloaded(URL url) {
		try {
			File cached = network.cacheLocation(new UnmodifiableUrl(url));
			if (cached.exists()) {
				return cached;
			}
			ensureDir(cached.getParentFile());
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

	public File unmodifiableZipUnzipped(UnmodifiableZip src) {
		try {
			File dest = network.cacheLocation(src);
			if (dest.exists()) {
				return dest;
			}
			log("unzipped", dest);
			ensureDir(dest);
			ZipInputStream zip = new ZipInputStream(src.location().openStream());
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
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public File unzippedSvnkit() {
		try {
			URL url = network.svnkitUrl();
			File cached = downloaded(url);
			File unzipped = unmodifiableZipUnzipped(new UnmodifiableZip(
					fileToUrl(cached)));
			return unzipped;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public URL svnkitUrl() {
		return network.svnkitUrl();
	}

	public File exportedFromSvn(URL url) {
		try {
			File exported = network.cacheLocation(new UnmodifiableUrl(url));
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
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isFile(URL url) {
		return "file".equals(url.getProtocol());
	}

	public static String withoutTrailingSlash(String string) {
		if (!string.endsWith("/")) {
			return string;
		}
		return string.substring(0, string.length() - 1);
	}
}
