package net.sf.iwant.entry3;

import java.io.File;
import java.net.URL;
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
			List<File> classLocations, boolean debug) {
		return iwant.compiledClasses(dest, src, classLocations, debug);
	}

	@Override
	public void downloaded(URL from, File to) {
		iwant.downloaded(from, to);
	}

}
