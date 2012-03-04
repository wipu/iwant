package net.sf.iwant.entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Iwant {

	public static void main(String[] args) throws IOException {
		File asSomeone = new File(args[0]);
		File iHave = new File(asSomeone, "i-have");
		iHave.mkdir();
		File iwantFrom = new File(iHave, "iwant-from");
		new FileWriter(iwantFrom).append("iwant-from=TODO\n").close();
		System.err.println("I created " + iwantFrom
				+ "\nPlease edit it and rerun me.");
		System.exit(1);
	}

	public String hello() {
		return "hello from iwant entry";
	}

}
