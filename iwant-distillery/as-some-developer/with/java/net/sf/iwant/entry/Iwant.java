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

	}

	private static class RealIwantNetwork implements IwantNetwork {

		private static final File HOME = new File(
				System.getProperty("user.home"));

		public File wantedUnmodifiable(URL url) {
			return new File(HOME, "/.net.sf.iwant/wanted-unmodifiable");
		}

		public URL svnkitUrl() {
			try {
				return new URL(
						"http://www.svnkit.com/org.tmatesoft.svn_1.3.5.standalone.nojna.zip");
			} catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			}
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
		URL iwantLocation = new URL(iwantFromProps.getProperty("iwant-from"));

		File iwantWs = exportedFromSvn(iwantLocation);

		File iwantBootstrapClasses = iwantBootstrapperClasses(iwantWs);
		runJavaMain(false, false, "net.sf.iwant.entry2.Iwant2",
				new File[] { iwantBootstrapClasses }, args);
	}

	private File iwantBootstrapperClasses(File iwantWs) throws IOException {
		File bsClasses = new File(iwantWs, "bootstrap-classes");
		ensureDir(bsClasses);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticListener<? super JavaFileObject> diagnosticListener = null;
		Locale locale = null;
		Charset charset = null;
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				diagnosticListener, locale, charset);
		Iterable<? extends JavaFileObject> compilationUnits = iwantBootstrappingJavaSources(
				iwantWs, fileManager);
		Writer compilerTaskOut = null;
		Iterable<String> options = Arrays.asList(new String[] { "-d",
				bsClasses.getCanonicalPath() });
		Iterable<String> classes = null;
		CompilationTask compilerTask = compiler.getTask(compilerTaskOut,
				fileManager, diagnosticListener, options, classes,
				compilationUnits);
		Boolean compilerTaskResult = compilerTask.call();
		fileManager.close();
		if (!compilerTaskResult) {
			throw new IwantException("Compilation failed.");
		}
		return bsClasses;
	}

	private static void debugLog(String task, Object... lines) {
		StringBuilder b = new StringBuilder();
		for (Object part : lines) {
			b.append(String.format("(%16s    ", task));
			b.append(part);
			b.append(")\n");
		}
		System.err.print(b);
	}

	private static void log(String task, File target) {
		StringBuilder b = new StringBuilder();
		b.append(String.format(":%16s -> ", task));
		b.append(target.getName());
		System.err.println(b);
	}

	private Iterable<? extends JavaFileObject> iwantBootstrappingJavaSources(
			File iwantWs, StandardJavaFileManager fileManager) {
		File iwant2 = new File(iwantWs,
				"iwant-distillery/src/main/java/net/sf/iwant/entry2/Iwant2.java");
		File iwant = new File(iwantWs,
				"iwant-distillery/as-some-developer/with/java/net/sf/iwant/entry/Iwant.java");
		debugLog("javac" + Arrays.asList(iwant2, iwant));

		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjects(iwant2, iwant);
		return compilationUnits;
	}

	private void runJavaMain(boolean outToErr, boolean catchSystemExit,
			String className, File[] classLocations, String... args)
			throws Exception {
		debugLog("invoke", "Running " + className, Arrays.toString(args),
				Arrays.toString(classLocations));
		ClassLoader classLoader = classLoader(classLocations);
		Class<?> helloClass = classLoader.loadClass(className);
		Method mainMethod = helloClass.getMethod("main", String[].class);

		Object[] invocationArgs = { args };

		SecurityManager origSecman = System.getSecurityManager();
		PrintStream origOut = System.out;
		PrintStream origErr = System.err;

		if (catchSystemExit) {
			System.setSecurityManager(new ExitCatcher());
		}
		if (outToErr) {
			System.setOut(origErr);
		}
		try {
			mainMethod.invoke(null, invocationArgs);
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

	private static URLClassLoader classLoader(File... locations)
			throws MalformedURLException {
		URL[] urls = new URL[locations.length];
		for (int i = 0; i < locations.length; i++) {
			urls[i] = locations[i].toURI().toURL();
		}
		return new URLClassLoader(urls, new ClassLoaderThatHidesIwant());
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
			runJavaMain(true, true, "org.tmatesoft.svn.cli.SVN", new File[] {
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
