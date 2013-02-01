package net.sf.iwant.testing;

import java.io.File;

public class WsRootFinder {

	public static File wsRoot() {
		try {
			File marker = new File(WsRootFinder.class.getResource(
					"/iwant-distillery-marker.txt").toURI());
			return marker.getParentFile().getParentFile().getParentFile();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static File mockWsRoot() {
		return new File(wsRoot(), "iwant-mock-wsroot");
	}

}
