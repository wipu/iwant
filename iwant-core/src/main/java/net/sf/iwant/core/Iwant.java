package net.sf.iwant.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Iwant {

	private static class IwantException extends Exception {

		public IwantException(String message) {
			super(message);
		}

	}

	public static void main(String[] args) throws IOException {
		try {
			targetAsPath(args);
		} catch (IwantException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private static void targetAsPath(String[] args) throws IwantException,
			IOException {
		File iHave = new File(args[0], "i-have");
		if (!iHave.exists()) {
			throw new IllegalStateException("Internal error: missing " + iHave);
		}
		File wsInfo = new File(iHave, "ws-info.conf");
		if (!wsInfo.exists()) {
			new FileWriter(wsInfo).append(
					"# paths are relative to this file's directory\n"
							+ "WSNAME=example\n" + "WSROOT=../..\n"
							+ "WSDEF_SRC=../i-have/wsdef\n"
							+ "WSDEF_CLASS=com.example.wsdef.Workspace\n")
					.close();
			throw new IwantException("I created " + wsInfo + " for you."
					+ " Please edit it and rerun me.");
		}
		System.err.println(iHave);
	}

}
