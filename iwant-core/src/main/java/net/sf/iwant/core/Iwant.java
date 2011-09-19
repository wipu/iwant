package net.sf.iwant.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Iwant {

	public static void main(String[] args) throws IOException {
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
			throw new IllegalStateException("I created " + wsInfo + " for you."
					+ " Please edit it and rerun me.");
		}
		System.err.println(iHave);
	}

}
