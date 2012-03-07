package net.sf.iwant.entry;

import java.io.File;

class WsRootFinder {

	static File wsRoot() {
		try {
			File marker = new File(WsRootFinder.class.getResource(
					"/iwant-distillery-marker.txt").toURI());
			return marker.getParentFile().getParentFile().getParentFile();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	static File mockWsRoot() {
		return new File(wsRoot(), "iwant-mock-wsroot");
	}

}
