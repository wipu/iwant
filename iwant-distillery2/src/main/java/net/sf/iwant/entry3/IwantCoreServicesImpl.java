package net.sf.iwant.entry3;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import net.sf.iwant.api.model.IwantCoreServices;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.io.StreamUtil;

public class IwantCoreServicesImpl implements IwantCoreServices {

	private Iwant iwant;

	public IwantCoreServicesImpl(Iwant iwant) {
		this.iwant = iwant;
	}

	@Override
	public File compiledClasses(File dest, List<File> src,
			List<File> classLocations, boolean debug) {
		return iwant.compiledClasses(dest, src, classLocations, debug);
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
