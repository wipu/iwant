package org.fluentjava.iwant.iwantwsrootfinder;

import java.io.File;

public class IwantWsRootFinder {

	public static File essential() {
		try {
			File marker = new File(IwantWsRootFinder.class
					.getResource("/iwant-wsroot-marker.txt").toURI());
			return marker.getParentFile().getParentFile();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static File mockWsRoot() {
		return new File(essential().getParentFile(),
				"private/iwant-mock-wsroot");
	}

	public static File mockEssential() {
		return new File(mockWsRoot(), "essential");
	}

}
