package org.fluentjava.iwant.entry3;

public class WishScriptGenerator {

	public static String wishScriptContent(String wish) {
		StringBuilder b = new StringBuilder();
		b.append("#!/bin/bash\n");
		b.append("HERE=$(dirname \"$0\")\n");
		b.append("exec \"$HERE/" + doubleDots(wish) + "help.sh\" \"" + wish
				+ "\"\n");
		return b.toString();
	}

	private static String doubleDots(String wish) {
		StringBuilder b = new StringBuilder();
		int slashCount = wish.split("/").length - 1;
		for (int i = 0; i < slashCount; i++) {
			b.append("../");
		}
		return b.toString();
	}

}
