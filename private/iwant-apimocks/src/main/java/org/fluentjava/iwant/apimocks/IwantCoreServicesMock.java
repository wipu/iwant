package org.fluentjava.iwant.apimocks;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.fluentjava.iwant.api.model.IwantCoreServices;

public class IwantCoreServicesMock implements IwantCoreServices {

	private final IwantCoreServices delegate;
	private File taughtCygwinBashExe;
	private boolean cygwinBashExeWasTaught;
	private boolean shallMockWintoySafePaths;
	private List<String> lastJavacOptions;

	public IwantCoreServicesMock(IwantCoreServices delegate) {
		this.delegate = delegate;
	}

	@Override
	public File compiledClasses(File dest, List<File> src,
			List<File> classLocations, List<String> javacOptions,
			Charset encoding) {
		lastJavacOptions = new ArrayList<>(javacOptions);
		return delegate.compiledClasses(dest, src, classLocations, javacOptions,
				encoding);
	}

	public List<String> lastJavacOptions() {
		return lastJavacOptions;
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
			return "only-slashes:" + delegate.pathWithoutBackslashes(file);
		}
		return delegate.pathWithoutBackslashes(file);
	}

	@Override
	public String unixPathOf(File file) {
		if (shallMockWintoySafePaths) {
			return "mock-unix-path:" + delegate.pathWithoutBackslashes(file);
		}
		return delegate.unixPathOf(file);
	}

	public void shallNotMockWintoySafePaths() {
		this.shallMockWintoySafePaths = false;
	}

	public void shallMockWintoySafePaths() {
		this.shallMockWintoySafePaths = true;
	}

}
