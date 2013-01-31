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
import java.util.Properties;

import net.sf.iwant.api.ExternalSource;
import net.sf.iwant.api.HelloSideEffect;
import net.sf.iwant.api.HelloTarget;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.JavaClasses;
import net.sf.iwant.api.JavaModule;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.SideEffect;
import net.sf.iwant.api.SideEffectDefinitionContext;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.api.WsInfo;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.WsRootFinder;
import net.sf.iwant.io.StreamUtil;

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
		File iHaveConf = new File(iHave, "conf");
		File wsInfoFile = wsInfoFile(iHaveConf);
		WsInfo wsInfo = parseWsInfo(wsInfoFile);
		if (!wsInfo.wsdefdefJava().exists()) {
			throw createExampleWsdefdefAndWsdef(asSomeone, iHave, wsInfo);
		}
		UserPrefs userPrefs = parseUserPrefs(iHaveConf);

		File wsCache = new File(asSomeone, ".i-cached");
		CachesImpl caches = new CachesImpl(wsCache, wsInfo.wsRoot(),
				iwant.network());
		File wsDefdefClasses = new File(wsCache, "wsdefdef-classes");

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
			JavaModule wsdDefClassesModule = wsDefdef
					.workspaceModule(JavaModule
							.implicitLibrary(new ExternalSource(iwantApiClasses)));
			// TODO don't cast when no more necessary
			JavaClasses wsDefClassesTarget = (JavaClasses) wsdDefClassesModule
					.mainClasses();

			String wsdefdefRelativeToWsRoot = FileUtil
					.relativePathOfFileUnderParent(wsInfo.wsdefdefModule(),
							wsInfo.wsRoot());
			JavaModule wsdefdefJavaModule = JavaModule
					.with()
					.name(wsInfo.wsName() + "-wsdefdef")
					.locationUnderWsRoot(wsdefdefRelativeToWsRoot)
					.mainJava("src/main/java")
					.mainDeps(
							JavaModule.implicitLibrary(new ExternalSource(
									iwantApiClasses))).end();
			WishEvaluator wishEvaluator = new WishEvaluator(System.out,
					System.err, wsInfo.wsRoot(), iwant, wsInfo, caches,
					userPrefs.workerCount(), wsdefdefJavaModule,
					wsdDefClassesModule);

			File wsDefClasses = wishEvaluator
					.freshCachedContent(wsDefClassesTarget);

			Iwant.fileLog("Classloading wsdef");
			Class<?> wsDefClass = loadClass(
					getClass().getClassLoader(),
					wsDefdef.workspaceClassname(),
					wsdefRuntimeClasspath(
							wishEvaluator.targetEvaluationContext(),
							wsDefClassesTarget, wsDefdefClasses, wsDefClasses));
			Iwant.fileLog("Instantiating " + wsDefClass);
			IwantWorkspace wsDef = (IwantWorkspace) wsDefClass.newInstance();
			refreshWishScripts(asSomeone, wsDef,
					wishEvaluator.sideEffectDefinitionContext());
			if (args.length == 0) {
				throw new IwantException(
						"(Using "
								+ userPrefs
								+ ")\nTry "
								+ new File(asSomeone,
										"with/bash/iwant/list-of/targets"));
			}
			String wish = args[0];
			Iwant.fileLog("Wanting " + wish + " from " + wsDef);
			wishEvaluator.iwant(wish, wsDef);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("Error invoking user code.", e);
		}
	}

	private static UserPrefs parseUserPrefs(File iHaveConf) throws IOException {
		File userPrefsFile = new File(iHaveConf, "user-preferences");
		if (!userPrefsFile.exists()) {
			return new DefaultUserPrefs(userPrefsFile);
		}
		FileReader reader = new FileReader(userPrefsFile);
		try {
			Properties props = new Properties();
			props.load(reader);
			return new UserPrefsImpl(props, userPrefsFile);
		} finally {
			reader.close();
		}
	}

	private static IwantException createExampleWsdefdefAndWsdef(File asSomeone,
			File iHave, WsInfo wsInfo) {
		File iwantWsRoot = WsRootFinder.wsRoot();
		createFile(
				wsInfo.wsdefdefJava(),
				ExampleWsDefGenerator.exampleWsdefdef(iwantWsRoot,
						wsInfo.wsdefdefPackage(),
						wsInfo.wsdefdefClassSimpleName(), wsInfo.wsName()));
		File wsDefJava = new File(iHave, "/wsdef/src/main/java"
				+ "/com/example/wsdef/Workspace.java");
		createFile(wsDefJava, ExampleWsDefGenerator.exampleWsdef(iwantWsRoot,
				"com.example.wsdef", "Workspace"));
		// TODO it's a bit ugly to create dummy target and side-effect just to
		// get proper names for wish scripts:
		refreshWishScripts(asSomeone,
				Arrays.asList(new HelloTarget("hello", "not needed")),
				Arrays.asList(new HelloSideEffect("eclipse-settings")));
		IwantException e = new IwantException("I created\n"
				+ wsInfo.wsdefdefJava() + "\nand\n" + wsDefJava
				+ "\nPlease edit them and rerun me.");
		return e;
	}

	static List<File> wsdefRuntimeClasspath(TargetEvaluationContext ctx,
			JavaClasses wsdDefClassesTarget, File wsDefdefClasses,
			File wsDefClasses) {
		List<File> cp = new ArrayList<File>();
		cp.add(wsDefdefClasses);
		cp.add(wsDefClasses);
		for (Path extra : wsdDefClassesTarget.classLocations()) {
			cp.add(ctx.cached(extra));
		}
		return cp;
	}

	private static void refreshWishScripts(File asSomeone,
			IwantWorkspace wsDef, SideEffectDefinitionContext sedCtx) {
		refreshWishScripts(asSomeone, wsDef.targets(),
				wsDef.sideEffects(sedCtx));
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

	private static void refreshWishScripts(File asSomeone,
			Collection<? extends Target> targets,
			Collection<? extends SideEffect> sideEffects) {
		Iwant.fileLog("Refreshing wish scripts under " + asSomeone);
		File withBashIwant = new File(asSomeone, "with/bash/iwant");
		deleteWishScripts(withBashIwant);
		createWishScript(withBashIwant, "list-of/targets");
		for (Object target : targets) {
			createWishScript(withBashIwant, "target/" + target + "/as-path");
		}
		createWishScript(withBashIwant, "list-of/side-effects");
		for (SideEffect sideEffect : sideEffects) {
			createWishScript(withBashIwant, "side-effect/" + sideEffect.name()
					+ "/effective");
		}
	}

	private static void deleteWishScripts(File withBashIwant) {
		for (File child : withBashIwant.listFiles()) {
			if ("help.sh".equals(child.getName())) {
				continue;
			}
			Iwant.del(child);
		}
	}

	private static void createWishScript(File withBashIwant, String wish) {
		createScript(new File(withBashIwant, wish),
				WishScriptGenerator.wishScriptContent(wish));
	}

	private static File wsInfoFile(File iHaveConf) {
		File wsInfoFile = new File(iHaveConf, "ws-info");
		if (wsInfoFile.exists()) {
			return wsInfoFile;
		}
		createExampleWsInfo(wsInfoFile);
		throw new IwantException("I created " + wsInfoFile
				+ "\nPlease edit it and rerun me.");
	}

	private static WsInfo parseWsInfo(File wsInfoFile) throws IOException {
		FileReader in = null;
		try {
			in = new FileReader(wsInfoFile);
			return new WsInfoFileImpl(in, wsInfoFile);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Sorry, for a while I thought "
					+ wsInfoFile + " exists.");
		} finally {
			StreamUtil.tryToClose(in);
		}
	}

	private static void createExampleWsInfo(File wsInfo) {
		createFile(wsInfo, "# paths are relative to this file's directory\n"
				+ "WSNAME=example\n" + "WSROOT=../../..\n"
				+ "WSDEFDEF_MODULE=../wsdefdef\n"
				+ "WSDEFDEF_CLASS=com.example.wsdefdef.WorkspaceProvider\n");
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
