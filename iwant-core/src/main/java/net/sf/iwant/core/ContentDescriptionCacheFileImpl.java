package net.sf.iwant.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class ContentDescriptionCacheFileImpl implements ContentDescriptionCache {

	private final String cacheDir;

	public ContentDescriptionCacheFileImpl(String cacheDir) {
		this.cacheDir = cacheDir;
	}

	@Override
	public void cacheContentDescription(Target<?> target) throws IOException {
		String filename = cacheNameFor(target);
		TextOutput.debugLog("Caching content description of " + target + " to "
				+ filename);
		new FileWriter(filename, false).append(
				target.content().definitionDescription()).close();
	}

	private String cacheNameFor(Target<?> target) {
		return cacheDir + "/" + target.name();
	}

	@Override
	public String retrieveContentDescription(Target<?> target)
			throws IOException {
		File file = new File(cacheNameFor(target));
		if (!file.exists()) {
			return null;
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder b = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			b.append(line).append("\n");
			// TODO if the last line does not contain newline, we'll always get
			// a diff
		}
		reader.close();
		return b.toString();
	}

}
