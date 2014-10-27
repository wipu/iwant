package net.sf.iwant.apimocks;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.iwant.api.model.IwantCoreServices;
import net.sf.iwant.entry.Iwant;

public class IwantCoreServicesMock implements IwantCoreServices {

	private final IwantCoreServices delegate;
	private File taughtCygwinBashExe;
	private boolean cygwinBashExeWasTaught;
	private boolean shallMockWintoySafePaths;
	private final Map<URL, Integer> numberOfFilesToSvnExport = new HashMap<URL, Integer>();
	private final Map<URL, Integer> numberOfFilesToSvnExportBeforeFailure = new HashMap<URL, Integer>();

	public IwantCoreServicesMock(IwantCoreServices delegate) {
		this.delegate = delegate;
	}

	@Override
	public File compiledClasses(File dest, List<File> src,
			List<File> classLocations, boolean debug, Charset encoding) {
		return delegate.compiledClasses(dest, src, classLocations, debug,
				encoding);
	}

	@Override
	public int copyMissingFiles(File from, File to) {
		return delegate.copyMissingFiles(from, to);
	}

	@Override
	public void debugLog(String task, Object... lines) {
		delegate.debugLog(task, lines);
	}

	@Override
	public void downloaded(URL from, File to) {
		delegate.downloaded(from, to);
	}

	@Override
	public void svnExported(URL from, File to) {
		int fileCount = numberOfFilesToSvnExport.get(from);
		Integer failAfter = numberOfFilesToSvnExportBeforeFailure.get(from);
		to.mkdirs();
		for (int i = 0; i < fileCount; i++) {
			if (failAfter != null && i >= failAfter) {
				throw new IllegalStateException("Simulated svn export failure");
			}
			Iwant.newTextFile(new File(to, "exported-" + i),
					"content of exported-" + i);
		}
	}

	@Override
	public void pipe(InputStream in, OutputStream out) {
		delegate.pipe(in, out);
	}

	@Override
	public void pipeAndClose(InputStream in, OutputStream out) {
		delegate.pipeAndClose(in, out);
	}

	@Override
	public File cygwinBashExe() {
		if (cygwinBashExeWasTaught) {
			return taughtCygwinBashExe;
		}
		return delegate.cygwinBashExe();
	}

	public void shallFindCygwinBashExeAt(File cygwinBashExe) {
		this.taughtCygwinBashExe = cygwinBashExe;
		this.cygwinBashExeWasTaught = true;
	}

	public void shallNotFindCygwinBash() {
		this.taughtCygwinBashExe = null;
		this.cygwinBashExeWasTaught = true;
	}

	@Override
	public String pathWithoutBackslashes(File file) {
		if (shallMockWintoySafePaths) {
			return "only-slashes:" + file;
		}
		return delegate.pathWithoutBackslashes(file);
	}

	@Override
	public String unixPathOf(File file) {
		if (shallMockWintoySafePaths) {
			return "mock-unix-path:" + file;
		}
		return delegate.unixPathOf(file);
	}

	public void shallMockWintoySafePaths() {
		this.shallMockWintoySafePaths = true;
	}

	public void shallSvnExport(URL url, int numberOfFilesToSvnExport) {
		this.numberOfFilesToSvnExport.put(url, numberOfFilesToSvnExport);
	}

	public void shallFailSvnExportAfterFileCount(URL url,
			Integer numberOfFilesToSvnExportBeforeFailure) {
		this.numberOfFilesToSvnExportBeforeFailure.put(url,
				numberOfFilesToSvnExportBeforeFailure);
	}

}
