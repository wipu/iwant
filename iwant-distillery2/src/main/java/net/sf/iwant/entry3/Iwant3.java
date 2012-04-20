package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.WsRootFinder;

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

	public void evaluate(File asSomeone, String... args) {
		File iHave = new File(asSomeone, "i-have");
		Iwant.ensureDir(iHave);
		File wsInfoFile = wsInfoFile(iHave);
		WsInfo wsInfo = parseWsInfo(wsInfoFile);
		if (!wsInfo.wsdefJava().exists()) {
			createExampleWsdefJava(wsInfo);
			refreshWishScripts(asSomeone);
			throw new IwantException("I created " + wsInfo.wsdefJava()
					+ "\nPlease edit it and rerun me.");
		}
		String wish = args[0];
		if ("list-of/targets".equals(wish)) {
			System.out.println("hello");
		} else {
			System.out.println("todo path to hello");
		}
	}

	private void refreshWishScripts(File asSomeone) {
		File withBashIwant = new File(asSomeone, "with/bash/iwant");
		createWishScript(withBashIwant, "list-of/targets");
		createWishScript(withBashIwant, "target/hello/as-path");
	}

	private static void createWishScript(File withBashIwant, String wish) {
		createScript(new File(withBashIwant, wish),
				WishScriptGenerator.wishScriptContent(wish));
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

	private void createExampleWsdefJava(WsInfo wsInfo) {
		File iwantWsRoot = WsRootFinder.wsRoot();
		createFile(
				wsInfo.wsdefJava(),
				ExampleWsDefGenerator.example(iwantWsRoot,
						wsInfo.wsdefPackage(), wsInfo.wsdefClassSimpleName()));
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
		createFile(wsInfo, "# paths are relative to this file's directory\n"
				+ "WSNAME=example\n" + "WSROOT=../..\n" + "WSDEF_SRC=wsdef\n"
				+ "WSDEF_CLASS=com.example.wsdef.Workspace\n");
	}

	private static void createScript(File file, String content) {
		createFile(file, content);
		file.setExecutable(true);
	}

	private static void createFile(File file, String content) {
		try {
			Iwant.ensureDir(file.getParentFile());
			new FileWriter(file).append(content).close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
