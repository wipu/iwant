package net.sf.iwant.iwantwsrootfinder;

import java.io.File;

public class IwantWsRootFinder {

	public static File wsRoot() {
		try {
			File marker = new File(IwantWsRootFinder.class.getResource(
					"/iwant-wsroot-marker.txt").toURI());
			return marker.getParentFile().getParentFile();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static File mockWsRoot() {
		return new File(wsRoot(), "iwant-mock-wsroot");
	}

}