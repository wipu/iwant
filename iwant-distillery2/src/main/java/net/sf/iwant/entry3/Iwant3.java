package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.ExternalSource;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.JavaClasses;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.api.WsInfo;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.WsRootFinder;

public class Iwant3 {

	private final Iwant iwant;

	public Iwant3(IwantNetwork network) {
		this.iwant = Iwant.using(network);
	}

	public static void main(String[] args) throws Exception {
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

	public void evaluate(File asSomeone, String... args) throws Exception {
		File iHave = new File(asSomeone, "i-have");
		iHave.mkdirs();
		File wsInfoFile = wsInfoFile(iHave);
		WsInfo wsInfo = parseWsInfo(wsInfoFile);
		if (!wsInfo.wsdefdefJava().exists()) {
			File iwantWsRoot = WsRootFinder.wsRoot();
			String iHaveRelativeToWsroot = FileUtil
					.relativePathOfFileUnderParent(iHave, wsInfo.wsRoot());
			String wsdefSrcRelativeToWsRoot = iHaveRelativeToWsroot + "/wsdef";
			createFile(wsInfo.wsdefdefJava(),
					ExampleWsDefGenerator.exampleWsdefdef(iwantWsRoot,
							wsInfo.wsdefdefPackage(),
							wsInfo.wsdefdefClassSimpleName(),
							wsdefSrcRelativeToWsRoot));
			File wsDefJava = new File(wsInfo.wsRoot(), wsdefSrcRelativeToWsRoot
					+ "/com/example/wsdef/Workspace.java");
			createFile(wsDefJava, ExampleWsDefGenerator.exampleWsdef(
					iwantWsRoot, "com.example.wsdef", "Workspace"));
			refreshWishScripts(asSomeone, Arrays.asList("hello"),
					Arrays.asList("eclipse-settings"));
			throw new IwantException("I created\n" + wsInfo.wsdefdefJava()
					+ "\nand\n" + wsDefJava
					+ "\nPlease edit them and rerun me.");
		}
		if (args.length == 0) {
			throw new IwantException("Try "
					+ new File(asSomeone, "with/bash/iwant/list-of/targets"));
		}
		String wish = args[0];

		File cached = cached(asSomeone);
		File wsDefdefClasses = new File(cached, "wsdefdef-classes");

		List<File> srcFiles = Arrays.asList(wsInfo.wsdefdefJava());
		File iwantApiClasses = iwantApiClasses();
		iwant.compiledClasses(wsDefdefClasses, srcFiles,
				Arrays.asList(iwantApiClasses));

		List<File> runtimeClasses = Arrays.asList(wsDefdefClasses);
		Class<?> wsDefdefClass = loadClass(getClass().getClassLoader(),
				wsInfo.wsdefClass(), runtimeClasses);

		try {
			Iwant.fileLog("Calling wsdefdef");
			IwantWorkspaceProvider wsDefdef = (IwantWorkspaceProvider) wsDefdefClass
					.newInstance();

			Iwant.fileLog("Refreshing wsdef classes");
			JavaClasses wsdDefClassesTarget = wsDefdef
					.workspaceClasses(new ExternalSource(iwantApiClasses));

			WishEvaluator wishEvaluator = new WishEvaluator(System.out,
					System.err, asSomeone, wsInfo.wsRoot(), iwantApiClasses,
					iwant, wsInfo, wsdDefClassesTarget);

			File wsDefClasses = wishEvaluator
					.freshCachedContent(wsdDefClassesTarget);

			Iwant.fileLog("Calling wsdef");
			Class<?> wsDefClass = loadClass(
					getClass().getClassLoader(),
					wsDefdef.workspaceClassname(),
					wsdefRuntimeClasspath(
							wishEvaluator.targetEvaluationContext(),
							wsdDefClassesTarget, wsDefdefClasses, wsDefClasses));
			IwantWorkspace wsDef = (IwantWorkspace) wsDefClass.newInstance();
			refreshWishScripts(asSomeone, wsDef);
			wishEvaluator.iwant(wish, wsDef);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("Error invoking user code.", e);
		}
	}

	static List<File> wsdefRuntimeClasspath(TargetEvaluationContext ctx,
			JavaClasses wsdDefClassesTarget, File wsDefdefClasses,
			File wsDefClasses) {
		List<File> cp = new ArrayList<File>();
		cp.add(wsDefdefClasses);
		cp.add(wsDefClasses);
		for (Path extra : wsdDefClassesTarget.classLocations()) {
			cp.add(extra.cachedAt(ctx));
		}
		return cp;
	}

	private static void refreshWishScripts(File asSomeone, IwantWorkspace wsDef) {
		refreshWishScripts(asSomeone, wsDef.targets(),
				Arrays.asList("eclipse-settings"));
	}

	private static Class<?> loadClass(ClassLoader parent, String className,
			List<File> locations) {
		try {
			return Iwant.classLoader(parent, locations).loadClass(className);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static File iwantApiClasses() {
		try {
			URL url = Iwant3.class
					.getResource("/net/sf/iwant/api/IwantWorkspace.class");
			return new File(url.toURI()).getParentFile().getParentFile()
					.getParentFile().getParentFile().getParentFile();
		} catch (Exception e) {
			throw new IllegalStateException("Cannot find classes.", e);
		}
	}

	private static File cached(File iHave) {
		return new File(iHave, ".cached");
	}

	private static void refreshWishScripts(File asSomeone,
			Collection<?> targets, Collection<?> sideEffects) {
		File withBashIwant = new File(asSomeone, "with/bash/iwant");
		createWishScript(withBashIwant, "list-of/targets");
		for (Object target : targets) {
			createWishScript(withBashIwant, "target/" + target + "/as-path");
		}
		createWishScript(withBashIwant, "list-of/side-effects");
		for (Object sideEffect : sideEffects) {
			createWishScript(withBashIwant, "side-effect/" + sideEffect
					+ "/effective");
		}
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

	private static WsInfo parseWsInfo(File wsInfoFile) throws IOException {
		try {
			return new WsInfoFileImpl(new FileReader(wsInfoFile), wsInfoFile);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Sorry, for a while I thought "
					+ wsInfoFile + " exists.");
		}
	}

	private static void createExampleWsInfo(File wsInfo) {
		createFile(wsInfo, "# paths are relative to this file's directory\n"
				+ "WSNAME=example\n" + "WSROOT=../..\n"
				+ "WSDEF_SRC=wsdefdef\n"
				+ "WSDEF_CLASS=com.example.wsdefdef.WorkspaceProvider\n");
	}

	private static void createScript(File file, String content) {
		createFile(file, content);
		file.setExecutable(true);
	}

	private static void createFile(File file, String content) {
		try {
			file.getParentFile().mkdirs();
			new FileWriter(file).append(content).close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
