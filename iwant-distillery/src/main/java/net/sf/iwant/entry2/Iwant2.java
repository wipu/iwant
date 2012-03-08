package net.sf.iwant.entry2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;

public class Iwant2 {

	@SuppressWarnings("unused")
	private final IwantNetwork network;

	public Iwant2(IwantNetwork network) {
		this.network = network;
	}

	public static void main(String[] args) {
		File asSomeone = new File(args[0]);
		String[] args2 = new String[args.length - 1];
		System.arraycopy(args, 1, args2, 0, args2.length);
		try {
			using(Iwant.usingRealNetwork().network())
					.evaluate(asSomeone, args2);
		} catch (IwantException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public static Iwant2 using(IwantNetwork network) {
		return new Iwant2(network);
	}

	public static class IwantException extends RuntimeException {

		public IwantException(String message) {
			super(message);
		}

	}

	public void evaluate(File asSomeone,
			@SuppressWarnings("unused") String... args) {
		File iHave = new File(asSomeone, "i-have");
		Iwant.ensureDir(iHave);
		File wsInfo = new File(iHave, "ws-info");
		createExampleWsInfo(wsInfo);
		throw new IwantException("I created " + wsInfo
				+ "\nPlease edit it and rerun me.");
	}

	private static void createExampleWsInfo(File wsInfo) {
		try {
			new FileWriter(wsInfo).append(
					"# paths are relative to this file's directory\n"
							+ "WSNAME=example\n" + "WSROOT=../..\n"
							+ "WSDEF_SRC=wsdef\n"
							+ "WSDEF_CLASS=com.example.wsdef.Workspace\n")
					.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
