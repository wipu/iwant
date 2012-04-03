package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;

public class Iwant3 {

	@SuppressWarnings("unused")
	private final IwantNetwork network;

	public Iwant3(IwantNetwork network) {
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

	public static Iwant3 using(IwantNetwork network) {
		return new Iwant3(network);
	}

	public void evaluate(File asSomeone,
			@SuppressWarnings("unused") String... args) {
		File iHave = new File(asSomeone, "i-have");
		Iwant.ensureDir(iHave);
		File wsInfoFile = wsInfoFile(iHave);
		WsInfo wsInfo = parseWsInfo(wsInfoFile);
		createExampleWsdefJava(wsInfo);
		throw new IwantException("I created " + wsInfo.wsdefJava()
				+ "\nPlease edit it and rerun me.");
	}

	private static File wsInfoFile(File iHave) {
		File wsInfoFile = new File(iHave, "ws-info");
		if (wsInfoFile.exists()) {
			return wsInfoFile;
		}
		createExampleWsInfo(wsInfoFile);
		throw new IwantException("I created " + wsInfoFile
				+ "\nPlease edit it and rerun me.");
	}

	private static void createExampleWsdefJava(WsInfo wsInfo) {
		createExampleFile(wsInfo.wsdefJava(),
				"package " + wsInfo.wsdefPackage() + ";\n");
	}

	private static WsInfo parseWsInfo(File wsInfoFile) {
		try {
			return new WsInfo(new FileReader(wsInfoFile), wsInfoFile);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Sorry, for a while I thought "
					+ wsInfoFile + " exists.");
		}
	}

	private static void createExampleWsInfo(File wsInfo) {
		createExampleFile(wsInfo,
				"# paths are relative to this file's directory\n"
						+ "WSNAME=example\n" + "WSROOT=../..\n"
						+ "WSDEF_SRC=wsdef\n"
						+ "WSDEF_CLASS=com.example.wsdef.Workspace\n");
	}

	private static void createExampleFile(File file, String content) {
		try {
			Iwant.ensureDir(file.getParentFile());
			new FileWriter(file).append(content).close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
