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
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
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

		File wantedUnmodifiable();

		URL svnkitUrl();

	}

	private static class RealIwantNetwork implements IwantNetwork {

		private static final File HOME = new File(
				System.getProperty("user.home"));

		public File wantedUnmodifiable() {
			return new File(HOME, "/.net.sf.iwant/wanted-unmodifiable");
		}

		public URL svnkitUrl() {
			try {
				return new URL(
						"http://www.svnkit.com/org.tmatesoft.svn_1.3.5.standalone.nojna");
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
		String iwantLocation = iwantFromProps.getProperty("iwant-from");

		File wantedUnmodifiable = network.wantedUnmodifiable();
		File iwantWs = new File(wantedUnmodifiable, "iwant/"
				+ toSafeFilename(iwantLocation));

		File iwantBootstrapClasses = iwantBootstrapperClasses(iwantWs);
		runEntry2(iwantBootstrapClasses, args);
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
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjects(
						new File(iwantWs,
								"iwant-distillery/src/main/java/net/sf/iwant/entry2/Iwant2.java"),
						new File(iwantWs,
								"iwant-distillery/as-some-developer/with/java/net/sf/iwant/entry/Iwant.java"));
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

	private void runEntry2(File classes, String[] args) throws Exception {
		ClassLoader classLoader = classLoader(classes);
		Class<?> helloClass = classLoader
				.loadClass("net.sf.iwant.entry2.Iwant2");
		Method mainMethod = helloClass.getMethod("main", String[].class);

		Object[] invocationArgs = { args };
		mainMethod.invoke(null, invocationArgs);
	}

	private static URLClassLoader classLoader(File... locations)
			throws MalformedURLException {
		URL[] urls = new URL[locations.length];
		for (int i = 0; i < locations.length; i++) {
			urls[i] = locations[i].toURI().toURL();
		}
		return new URLClassLoader(urls);
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

	public File toCachePath(URL url) {
		return new File(network.wantedUnmodifiable(),
				toSafeFilename(url.toExternalForm()));
	}

	public File downloaded(URL url) {
		try {
			File cached = toCachePath(url);
			if (cached.exists()) {
				return cached;
			}
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

	public File unmodifiableZipUnzipped(String name, InputStream in) {
		try {
			File dest = new File(network.wantedUnmodifiable(), "unzipped/"
					+ toSafeFilename(name));
			if (dest.exists()) {
				return dest;
			}
			ensureDir(dest);
			ZipInputStream zip = new ZipInputStream(in);
			ZipEntry e = null;
			while ((e = zip.getNextEntry()) != null) {
				File entryFile = new File(dest, e.getName());
				if (e.isDirectory()) {
					ensureDir(entryFile);
					continue;
				}
				OutputStream out = new FileOutputStream(entryFile);
				while (true) {
					int i = zip.read();
					if (i == -1) {
						break;
					}
					out.write(i);
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
			File unzipped = unmodifiableZipUnzipped(url.toExternalForm(), in);
			return unzipped;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public URL svnkitUrl() {
		return network.svnkitUrl();
	}

	public File wantedUnmodifiable() {
		return network.wantedUnmodifiable();
	}

}
