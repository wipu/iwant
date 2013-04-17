package net.sf.iwant.coreservices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import net.sf.iwant.api.model.IwantCoreServices;
import net.sf.iwant.entry.Iwant;

public class IwantCoreServicesImpl implements IwantCoreServices {

	private Iwant iwant;

	public IwantCoreServicesImpl(Iwant iwant) {
		this.iwant = iwant;
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
	public void pipe(InputStream in, OutputStream out) {
		StreamUtil.pipe(in, out);
	}

	@Override
	public void pipeAndClose(InputStream in, OutputStream out) {
		StreamUtil.pipeAndClose(in, out);
	}

}
