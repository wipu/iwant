package net.sf.iwant.coreservices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import net.sf.iwant.api.model.IwantCoreServices;
import net.sf.iwant.entry.Iwant;

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
			List<File> classLocations, boolean debug, Charset encoding) {
		return iwant
				.compiledClasses(dest, src, classLocations, debug, encoding);
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
	public void svnExported(URL from, File to) {
		iwant.svnExport(from, to);
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
		if (!systemProperties.getProperty("os.name").startsWith("Windows")) {
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
		throw new IllegalStateException("Cannot find cygwin bash.exe");
	}

	@Override
	public String pathWithoutBackslashes(File file) {
		try {
			return file.getCanonicalPath().replaceAll("\\\\", "/");
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public String unixPathOf(File file) {
		return pathWithoutBackslashes(file).replaceFirst("^C:", "/cygdrive/c");
	}

}
