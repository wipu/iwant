package org.fluentjava.iwant.coreservices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import org.fluentjava.iwant.api.model.IwantCoreServices;
import org.fluentjava.iwant.entry.Iwant;

public class IwantCoreServicesImpl implements IwantCoreServices {

	private final Iwant iwant;
	private final File cRoot;
	private final Properties systemProperties;

	public IwantCoreServicesImpl(Iwant iwant, File cRoot,
			Properties systemProperties) {
		this.iwant = iwant;
		this.cRoot = cRoot;
		this.systemProperties = systemProperties;
	}

	public IwantCoreServicesImpl(Iwant iwant) {
		this(iwant, new File("C:"), System.getProperties());
	}

	@Override
	public File compiledClasses(File dest, List<File> src,
			List<File> classLocations, List<String> javacOptions,
			Charset encoding) {
		return iwant.compiledClasses(dest, src, classLocations, javacOptions,
				encoding);
	}

	@Override
	public int copyMissingFiles(File from, File to) {
		try {
			return FileUtil.copyMissingFiles(from, to);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void debugLog(String task, Object... lines) {
		Iwant.debugLog(task, lines);
	}

	@Override
	public void downloaded(URL from, File to) {
		iwant.downloaded(from, to);
	}

	@Override
	public void pipe(InputStream in, OutputStream out) {
		StreamUtil.pipe(in, out);
	}

	@Override
	public void pipeAndClose(InputStream in, OutputStream out) {
		StreamUtil.pipeAndClose(in, out);
	}

	@Override
	public File cygwinBashExe() {
		if (!runningWindows()) {
			return null;
		}
		File bash = new File(cRoot, "cygwin64/bin/bash.exe");
		if (bash.exists()) {
			return bash;
		}
		bash = new File(cRoot, "cygwin/bin/bash.exe");
		if (bash.exists()) {
			return bash;
		}
		bash = gitBash();
		if (bash.exists()) {
			return bash;
		}
		throw new IllegalStateException("Cannot find cygwin (or git) bash.exe");
	}

	private File gitBash() {
		File home = new File(systemProperties.getProperty("user.home"));
		return new File(home, "AppData/Local/Programs/Git/bin/bash.exe");
	}

	private boolean runningWindows() {
		return systemProperties.getProperty("os.name").startsWith("Windows");
	}

	@Override
	public String pathWithoutBackslashes(File file) {
		try {
			return absolutePathWithoutBackslashes(file.getCanonicalPath());
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static String absolutePathWithoutBackslashes(String absolutePath) {
		return absolutePath.replaceAll("\\\\", "/");
	}

	@Override
	public String unixPathOf(File file) {
		try {
			return toNativeBashFormat(file.getCanonicalPath());
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	String toNativeBashFormat(String absolutePath) {
		if (!runningWindows()) {
			return absolutePath;
		}
		String normalized = absolutePathWithoutBackslashes(absolutePath);
		// TODO don't assume cygwin is the only alternative to git-bash
		// TODO also don't assume the path starts with C:, it could as well be
		// D: ...
		String prefix = "/cygdrive/c";
		if (gitBash().exists()) {
			prefix = "/c";
		}
		return normalized.replaceFirst("^C:", prefix);
	}

}
