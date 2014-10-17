package net.sf.iwant.apimocks;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import net.sf.iwant.api.model.IwantCoreServices;

public class IwantCoreServicesMock implements IwantCoreServices {

	private final IwantCoreServices delegate;
	private File taughtCygwinBashExe;
	private boolean cygwinBashExeWasTaught;
	private boolean shallMockWintoySafePaths;
	private final Map<URL, File> executedSvnExports = new HashMap<URL, File>();

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
		executedSvnExports.put(from, to);
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

	public void shallHaveSvnExportedUrlTo(URL url, File exported) {
		Assert.assertEquals(exported, executedSvnExports.get(url));
	}

}
