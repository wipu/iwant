package net.sf.iwant.entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Iwant {

	public static void main(String[] args) {
		try {
			tryMain(args);
		} catch (IwantException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class IwantException extends RuntimeException {

		public IwantException(String message) {
			super(message);
		}

	}

	private static void tryMain(String[] args) throws IOException {
		if (args.length <= 0) {
			throw new IwantException("Usage: " + Iwant.class.getCanonicalName()
					+ " AS_SOMEONE_DIRECTORY [args...]");
		}
		File asSomeone = new File(args[0]);
		if (!asSomeone.exists()) {
			throw new IwantException("AS_SOMEONE_DIRECTORY does not exist: "
					+ asSomeone.getCanonicalPath());
		}
		File iHave = new File(asSomeone, "i-have");
		iHave.mkdir();
		File iwantFrom = new File(iHave, "iwant-from");
		new FileWriter(iwantFrom).append("iwant-from=TODO\n").close();
		System.err.println("I created " + iwantFrom
				+ "\nPlease edit it and rerun me.");
		System.exit(1);
	}

}
