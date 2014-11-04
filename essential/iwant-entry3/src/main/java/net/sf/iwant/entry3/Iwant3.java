package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.api.core.HelloSideEffect;
import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.api.model.WsInfo;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.IwantWorkspaceProvider;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.WorkspaceDefinitionContext;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.coreservices.StreamUtil;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.Iwant.UnmodifiableSource;
import net.sf.iwant.entry2.Iwant2;
import net.sf.iwant.iwantwsrootfinder.IwantWsRootFinder;

public class Iwant3 {

	private static final String WISH_LIST_OF_SIDE_EFFECTS = "list-of/side-effects";
	private static final String WISH_LIST_OF_TARGETS = "list-of/targets";
	private final Iwant iwant;
	private final File iwantEssential;

	public Iwant3(IwantNetwork network, File iwantEssential) {
		this.iwantEssential = iwantEssential;
		this.iwant = Iwant.using(network);
	}

	public static void main(String[] args) throws Exception {
		File iwantEssential = new File(args[0]);
		File asSomeone = new File(args[1]);
		String[] args2 = new String[args.length - 2];
		System.arraycopy(args, 2, args2, 0, args2.length);
		try {
			using(Iwant.usingRealNetwork().network(), iwantEssential).evaluate(
					asSomeone, args2);
		} catch (IwantException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public static Iwant3 using(IwantNetwork network, File iwantEssential) {
		return new Iwant3(network, iwantEssential);
	}

	private Set<JavaModule> iwantApiModules(SortedSet<File> apiClassLocations)
			throws IOException {
		Path combinedIwantSources = combinedIwantSources();
		SortedSet<JavaModule> iwantApiModules = new TreeSet<>();
		for (File apiClasses : apiClassLocations) {
			iwantApiModules
					.add(JavaBinModule.providing(
							new ExternalSource(apiClasses),
							combinedIwantSources).end());
		}
		return iwantApiModules;
	}

	public void evaluate(File asSomeone, String... args) throws Exception {
		doEvaluate(asSomeone.getCanonicalFile(), args);
	}

	private void doEvaluate(File asSomeone, String... args) throws Exception {
		File iHave = new File(asSomeone, "i-have");
		iHave.mkdirs();
		File iHaveConf = new File(iHave, "conf");
		File wsInfoFile = wsInfoFile(iHaveConf);
		WsInfo wsInfo = parseWsInfo(wsInfoFile, asSomeone);
		if (!wsInfo.wsdefdefJava().exists()) {
			throw createExampleWsdefdefAndWsdef(asSomeone, iHave, wsInfo);
		}
		UserPrefs userPrefs = parseUserPrefs(iHaveConf);

		File wsCache = new File(asSomeone, ".i-cached");
		CachesImpl caches = new CachesImpl(wsCache, wsInfo.wsRoot(),
				iwant.network());
		File wsDefdefClasses = new File(wsCache, "wsdefdef-classes");

		SortedSet<File> apiClassLocations = iwantApiClassLocations();
		Set<JavaModule> iwantApiModules = iwantApiModules(apiClassLocations);

		List<File> srcFiles = Arrays.asList(wsInfo.wsdefdefJava());
		iwant.compiledClasses(wsDefdefClasses, srcFiles, new ArrayList<>(
				apiClassLocations), Iwant.bootstrappingJavacOptions(), null);

		List<File> runtimeClasses = Arrays.asList(wsDefdefClasses);
		Class<?> wsDefdefClass = loadClass(getClass().getClassLoader(),
				wsInfo.wsdefClass(), runtimeClasses);

		try {
			Iwant.fileLog("Calling wsdefdef");
			IwantWorkspaceProvider wsDefdef = (IwantWorkspaceProvider) wsDefdefClass
					.newInstance();

			Iwant.fileLog("Refreshing wsdef classes");
			String wsdefdefRelativeToWsRoot = FileUtil
					.relativePathOfFileUnderParent(wsInfo.wsdefdefModule(),
							wsInfo.wsRoot());
			JavaSrcModule wsdefdefJavaModule = JavaSrcModule.with()
					.name(wsInfo.wsName() + "-wsdefdef")
					.locationUnderWsRoot(wsdefdefRelativeToWsRoot)
					.mainJava("src/main/java").mainDeps(iwantApiModules).end();
			URL iwantRootFromUrl = iwant.wishedIwantRootFromUrl(asSomeone);
			WorkspaceDefinitionContext wsDefCtx = new WorkspaceDefinitionContextImpl(
					iwantApiModules, iwantRootFromUrl, wsdefdefJavaModule);
			JavaSrcModule wsdDefClassesModule = wsDefdef
					.workspaceModule(wsDefCtx);
			// TODO don't cast when no more necessary
			JavaClasses wsDefClassesTarget = (JavaClasses) wsdDefClassesModule
					.mainArtifact();

			WishEvaluator wishEvaluator = new WishEvaluator(System.out,
					System.err, wsInfo.wsRoot(), iwant, wsInfo, caches,
					userPrefs.workerCount(), wsdefdefJavaModule,
					wsdDefClassesModule, wsDefCtx);

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
				throw new IwantException("(Using "
						+ userPrefs
						+ ")\nTry "
						+ new File(withBashIwantFile(asSomeone),
								WISH_LIST_OF_SIDE_EFFECTS)
						+ "\nor\n"
						+ new File(withBashIwantFile(asSomeone),
								WISH_LIST_OF_TARGETS));
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

	private Path combinedIwantSources() throws IOException {
		File combinedSources = iwant.network().cacheLocation(
				new CombinedSrcFromUnmodifiableIwantEssential(iwantEssential));
		Iwant.fileLog("Combining iwant sources from " + iwantEssential + " to "
				+ combinedSources);
		combinedSources.mkdirs();
		SortedSet<File> srcDirs = Iwant2.srcDirsOfIwantWs(iwantEssential);
		for (File srcDir : srcDirs) {
			FileUtil.copyMissingFiles(srcDir, combinedSources);
		}
		return new ExternalSource(combinedSources);
	}

	static class CombinedSrcFromUnmodifiableIwantEssential extends
			UnmodifiableSource<File> {

		public CombinedSrcFromUnmodifiableIwantEssential(File location) {
			super(location);
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
		File iwantWsRoot = IwantWsRootFinder.wsRoot();
		FileUtil.newTextFile(
				wsInfo.wsdefdefJava(),
				ExampleWsDefGenerator.exampleWsdefdef(iwantWsRoot,
						wsInfo.wsdefdefPackage(),
						wsInfo.wsdefdefClassSimpleName(), wsInfo.wsName()));
		File wsDefJava = new File(iHave, "/wsdef/src/main/java"
				+ "/com/example/wsdef/Workspace.java");
		FileUtil.newTextFile(wsDefJava, ExampleWsDefGenerator.exampleWsdef(
				iwantWsRoot, "com.example.wsdef", "Workspace"));
		// TODO it's a bit ugly to create dummy target and side-effect just to
		// get proper names for wish scripts:
		HelloSideEffect stubEclipseSettingsSe = new HelloSideEffect(
				"eclipse-settings");
		refreshWishScripts(asSomeone,
				Arrays.asList(new HelloTarget("hello", "not needed")),
				Arrays.asList(stubEclipseSettingsSe));
		IwantException e = new IwantException(
				"I created\n"
						+ wsInfo.wsdefdefJava()
						+ "\nand\n"
						+ wsDefJava
						+ "\nPlease edit them and rerun me.\nIf you want to use Eclipse for editing, run "
						+ new File(withBashIwantFile(asSomeone),
								toWish(stubEclipseSettingsSe)) + " first.");
		return e;
	}

	static List<File> wsdefRuntimeClasspath(TargetEvaluationContext ctx,
			JavaClasses wsdDefClassesTarget, File wsDefdefClasses,
			File wsDefClasses) {
		List<File> cp = new ArrayList<>();
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

	private static SortedSet<File> iwantApiClassLocations()
			throws URISyntaxException {
		SortedSet<File> apiClassLocations = new TreeSet<>();
		apiClassLocations.add(classesDirOf("/net/sf/iwant/"
				+ "api/core/HelloTarget.class"));
		apiClassLocations.add(classesDirOf("/net/sf/iwant/"
				+ "api/javamodules/JavaModule.class"));
		apiClassLocations.add(classesDirOf("/net/sf/iwant/"
				+ "api/model/Path.class"));
		apiClassLocations.add(classesDirOf("/net/sf/iwant/"
				+ "api/wsdef/IwantWorkspace.class"));
		// TODO maybe make eclipse-settings an optional plugin
		apiClassLocations.add(classesDirOf("/net/sf/iwant/"
				+ "eclipsesettings/EclipseSettings.class"));
		return apiClassLocations;
	}

	private static File classesDirOf(String resourceInsideClassLocation)
			throws URISyntaxException {
		int slashCount = countOfOccurrencesIn('/', resourceInsideClassLocation);
		URL url = Iwant3.class.getResource(resourceInsideClassLocation);
		File file = new File(url.toURI());
		for (int i = 0; i < slashCount; i++) {
			file = file.getParentFile();
		}
		return file;
	}

	private static int countOfOccurrencesIn(char c, String string) {
		int count = 0;
		for (int i = 0; i < string.length(); i++) {
			if (c == string.charAt(i)) {
				count++;
			}
		}
		return count;
	}

	private static void refreshWishScripts(File asSomeone,
			Collection<? extends Target> targets,
			Collection<? extends SideEffect> sideEffects) {
		Iwant.fileLog("Refreshing wish scripts under " + asSomeone);
		File withBashIwant = withBashIwantFile(asSomeone);
		deleteWishScripts(withBashIwant);
		createWishScript(withBashIwant, WISH_LIST_OF_TARGETS);
		for (Object target : targets) {
			createWishScript(withBashIwant, "target/" + target + "/as-path");
		}
		createWishScript(withBashIwant, WISH_LIST_OF_SIDE_EFFECTS);
		for (SideEffect sideEffect : sideEffects) {
			createWishScript(withBashIwant, toWish(sideEffect));
		}
	}

	private static String toWish(SideEffect sideEffect) {
		return "side-effect/" + sideEffect.name() + "/effective";
	}

	private static File withBashIwantFile(File asSomeone) {
		return new File(asSomeone, "with/bash/iwant");
	}

	private static void deleteWishScripts(File withBashIwant) {
		for (File child : withBashIwant.listFiles()) {
			if (isWishScriptParent(child)) {
				Iwant.del(child);
			}
		}
	}

	private static boolean isWishScriptParent(File dir) {
		String name = dir.getName();
		return "target".equals(name) || "side-effect".equals(name);
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

	private static WsInfo parseWsInfo(File wsInfoFile, File asSomeone)
			throws IOException {
		FileReader in = null;
		try {
			in = new FileReader(wsInfoFile);
			return new WsInfoFileImpl(in, wsInfoFile, asSomeone);
		} finally {
			StreamUtil.tryToClose(in);
		}
	}

	private static void createExampleWsInfo(File wsInfo) {
		FileUtil.newTextFile(
				wsInfo,
				"# paths are relative to this file's directory\n"
						+ "WSNAME=example\n"
						+ "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.WorkspaceProvider\n");
	}

	private static void createScript(File file, String content) {
		FileUtil.newTextFile(file, content);
		file.setExecutable(true);
	}

}
