package net.sf.iwant.core;

import java.io.File;
import java.io.IOException;

public class Locations {

	private final String wsRoot;
	private final String asSomeone;
	private final String cacheDir;
	private final String iwantLibs;

	public Locations(String wsRoot, String asSomeone, String cacheDir,
			String iwantLibs) {
		this.wsRoot = wsRoot;
		this.asSomeone = asSomeone;
		this.cacheDir = cacheDir;
		this.iwantLibs = iwantLibs;
	}

	public static Locations from(File wsRoot, File iHave, String wsName,
			File iwantLibs) throws IOException {
		return new Locations(wsRoot.getCanonicalPath(),
				new File(iHave, "/..").getCanonicalPath(),
				new File(iHave, "../with/bash/iwant/cached/" + wsName)
						.getCanonicalPath(), iwantLibs.getCanonicalPath());

	}

	public String wsRoot() {
		return wsRoot;
	}

	public String asSomeone() {
		return asSomeone;
	}

	public String iHave() {
		return asSomeone + "/i-have";
	}

	public String iwant() {
		return asSomeone + "/with/bash/iwant";
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
		return "Locations {\n  wsRoot():" + wsRoot() + "\n  asSomeone():"
				+ asSomeone() + "\n  iHave():" + iHave() + "\n  iwant():"
				+ iwant() + "\n  cacheDir():" + cacheDir()
				+ "\n  targetCacheDir():" + targetCacheDir()
				+ "\n  contentDescriptionCacheDir():"
				+ contentDescriptionCacheDir() + "\n  temporaryDirectory():"
				+ temporaryDirectory() + "\n  iwantLibs():" + iwantLibs()
				+ "\n]";
	}

}
