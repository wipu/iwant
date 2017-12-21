package org.fluentjava.iwant.core;

import java.io.FileWriter;
import java.io.IOException;

/**
 * TODO TDD this to cover all output, prefixed when needed. Now this only
 * servers development-time logging.
 * 
 * TODO and make sure we don't fill the /tmp filesystem!
 */
class TextOutput {

	static {
		debugLog("Starting iwant JRT, iwant-print-prefix="
				+ PrintPrefixes.fromSystemProperty().prefix());
	}

	public static void debugLog(String message) {
		try {
			new FileWriter("/tmp/iwant.log", true)
					.append("JRT-")
					.append(Integer
							.toHexString(Runtime.getRuntime().hashCode()))
					.append(" | ").append(message).append("\n").close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
