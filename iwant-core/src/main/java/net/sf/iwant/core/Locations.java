package net.sf.iwant.core;

import java.io.File;
import java.io.IOException;

public class Locations {

	private final String wsRoot;
	private final String cacheDir;
	private final String iwantLibs;

	public Locations(String wsRoot, String cacheDir, String iwantLibs) {
		this.wsRoot = wsRoot;
		this.cacheDir = cacheDir;
		this.iwantLibs = iwantLibs;
	}

	public static Locations from(File wsRoot, File iHave, String wsName,
			File iwantLibs) throws IOException {
		return new Locations(wsRoot.getCanonicalPath(), new File(iHave,
				"../iwant/cached/" + wsName).getCanonicalPath(),
				iwantLibs.getCanonicalPath());

	}

	public String wsRoot() {
		return wsRoot;
	}

	public String cacheDir() {
		return cacheDir;
	}

	public String targetCacheDir() {
		return cacheDir + "/target";
	}

	public String contentDescriptionCacheDir() {
		return cacheDir + "/content-descr";
	}

	public String temporaryDirectory() {
		return cacheDir + "/tmp-for-the-only-worker-thread";
	}

	public String iwantLibs() {
		return iwantLibs;
	}

	@Override
	public String toString() {
		return "Locations {\n  wsRoot():" + wsRoot() + "\n  cacheDir():"
				+ cacheDir() + "\n  targetCacheDir():" + targetCacheDir()
				+ "\n  contentDescriptionCacheDir():"
				+ contentDescriptionCacheDir() + "\n  temporaryDirectory():"
				+ temporaryDirectory() + "\n  iwantLibs():" + iwantLibs()
				+ "\n]";
	}

}
