package org.fluentjava.iwant.entry3;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fluentjava.iwant.api.core.HelloSideEffect;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaClasses;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.model.WsInfo;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.api.wsdef.WorkspaceFactory;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleProvider;
import org.fluentjava.iwant.coreservices.FileUtil;
import org.fluentjava.iwant.coreservices.StreamUtil;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.fluentjava.iwant.entry.Iwant.IwantNetwork;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableSource;
import org.fluentjava.iwant.entry2.Iwant2;
import org.fluentjava.iwant.iwantwsrootfinder.IwantWsRootFinder;

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
			using(Iwant.usingRealNetwork().network(), iwantEssential)
					.evaluate(asSomeone, args2);
		} catch (IwantException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public static Iwant3 using(IwantNetwork network, File iwantEssential) {
		return new Iwant3(network, iwantEssential);
	}

	private Set<JavaModule> iwantApiModules(List<File> apiClassLocations)
			throws IOException {
		Path combinedIwantSources = combinedIwantSources();
		SortedSet<JavaModule> iwantApiModules = new TreeSet<>();
		for (File apiClasses : apiClassLocations) {
			iwantApiModules
					.add(JavaBinModule.providing(ExternalSource.at(apiClasses),
							combinedIwantSources).end());
		}
		return iwantApiModules;
	}

	public void evaluate(File asSomeone, String... args) throws Exception {
		doEvaluate(asSomeone.getCanonicalFile(), args);
	}

	private void doEvaluate(File asSomeone, String... args) throws Exception {
		File iHave = new File(asSomeone, "i-have");
		Iwant.mkdirs(iHave);
		File iHaveConf = new File(iHave, "conf");
		File wsInfoFile = wsInfoFile(iHaveConf);
		WsInfo wsInfo = parseWsInfo(wsInfoFile, asSomeone);
		if (!wsInfo.wsdefdefJava().exists()) {
			throw createExampleWsdefdefAndWsdefAndWs(asSomeone, iHave, wsInfo);
		}
		UserPrefs userPrefs = parseUserPrefs(iHaveConf);

		File wsCache = new File(asSomeone, ".i-cached");
		CachesImpl caches = new CachesImpl(wsCache, wsInfo.wsRoot(),
				iwant.network());
		File wsDefdefClasses = new File(wsCache, "wsdefdef-classes");

		List<File> apiClassLocations = iwantApiClassLocations();
		Set<JavaModule> iwantApiModules = iwantApiModules(apiClassLocations);
		List<File> srcFiles = Arrays.asList(wsInfo.wsdefdefJava());

		if (ingredientsChanged(wsDefdefClasses, srcFiles, apiClassLocations)) {
			iwant.compiledClasses(wsDefdefClasses, srcFiles,
					new ArrayList<>(apiClassLocations),
					Iwant.bootstrappingJavacOptions(), StandardCharsets.UTF_8);
		}

		List<File> runtimeClasses = Arrays.asList(wsDefdefClasses);
		Class<?> wsDefdefClass = loadClass(getClass().getClassLoader(),
				wsInfo.wsdefdefClass(), runtimeClasses);

		try {
			Iwant.fileLog("Calling wsdefdef");
			WorkspaceModuleProvider wsDefdef = (WorkspaceModuleProvider) wsDefdefClass
					.getDeclaredConstructor().newInstance();

			Iwant.fileLog("Refreshing wsdef classes");
			String wsdefdefRelativeToWsRoot = FileUtil
					.relativePathOfFileUnderParent(wsInfo.wsdefdefModule(),
							wsInfo.wsRoot());
			JavaSrcModule wsdefdefJavaModule = JavaSrcModule.with()
					.name(wsInfo.wsName() + "-wsdefdef")
					.locationUnderWsRoot(wsdefdefRelativeToWsRoot)
					.mainJava("src/main/java").mainDeps(iwantApiModules).end();
			File iwantSrcRoot = iwant.iwantSourceOfWishedVersion(asSomeone);
			WorkspaceModuleContext wsDefCtx = new WorkspaceDefinitionContextImpl(
					iwantApiModules, iwantSrcRoot, wsdefdefJavaModule);
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

			Iwant.fileLog("Classloading workspace factory");
			Class<?> wsDefClass = loadClass(getClass().getClassLoader(),
					wsDefdef.workspaceFactoryClassname(),
					wsdefRuntimeClasspath(
							wishEvaluator.targetEvaluationContext(),
							wsDefClassesTarget, wsDefdefClasses, wsDefClasses));
			Iwant.fileLog("Instantiating " + wsDefClass);
			WorkspaceFactory wsDef = (WorkspaceFactory) wsDefClass
					.getDeclaredConstructor().newInstance();
			Workspace ws = wsDef.workspace(wishEvaluator.workspaceContext());
			refreshWishScripts(asSomeone, ws,
					wishEvaluator.targetDefinitionContext(),
					wishEvaluator.sideEffectDefinitionContext());
			if (args.length == 0) {
				throw new IwantException("(Using " + userPrefs + ")\nTry "
						+ new File(withBashIwantFile(asSomeone),
								WISH_LIST_OF_SIDE_EFFECTS)
						+ "\nor\n" + new File(withBashIwantFile(asSomeone),
								WISH_LIST_OF_TARGETS));
			}
			String wish = args[0];
			Iwant.fileLog("Wanting " + wish + " from " + ws);
			wishEvaluator.iwant(wish, ws);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("Error invoking user code.", e);
		}
	}

	private static boolean ingredientsChanged(File target, List<File> srcDeps,
			List<File> classDeps) {
		if (!target.exists()) {
			return true;
		}
		long ts = target.lastModified();
		return Iwant.isModifiedSince(srcDeps, ts)
				|| Iwant.isModifiedSince(classDeps, ts);
	}

	private Path combinedIwantSources() throws IOException {
		File combinedSources = iwant.network().cacheOfContentFrom(
				new CombinedSrcFromUnmodifiableIwantEssential(iwantEssential));
		Iwant.fileLog("Combining iwant sources from " + iwantEssential + " to "
				+ combinedSources);
		Iwant.mkdirs(combinedSources);
		SortedSet<File> srcDirs = Iwant2.srcDirsOfIwantWs(iwantEssential);
		for (File srcDir : srcDirs) {
			FileUtil.copyMissingFiles(srcDir, combinedSources);
		}
		return ExternalSource.at(combinedSources);
	}

	public static class CombinedSrcFromUnmodifiableIwantEssential
			extends UnmodifiableSource<File> {

		public CombinedSrcFromUnmodifiableIwantEssential(File location) {
			super(location);
		}

	}

	private static UserPrefs parseUserPrefs(File iHaveConf) throws IOException {
		File userPrefsFile = new File(iHaveConf, "user-preferences");
		if (!userPrefsFile.exists()) {
			return new DefaultUserPrefs(userPrefsFile);
		}
		try (FileReader reader = new FileReader(userPrefsFile)) {
			Properties props = new Properties();
			props.load(reader);
			return new UserPrefsImpl(props, userPrefsFile);
		}
	}

	private static IwantException createExampleWsdefdefAndWsdefAndWs(
			File asSomeone, File iHave, WsInfo wsInfo) {
		File essential = IwantWsRootFinder.essential();
		String wsDefPackage = ExampleWsDefGenerator
				.proposedWsdefPackage(wsInfo.wsdefdefPackage());
		String wsDefName = ExampleWsDefGenerator
				.proposedWsdefSimpleName(wsInfo.wsName());
		FileUtil.textFileEnsuredToHaveContent(wsInfo.wsdefdefJava(),
				ExampleWsDefGenerator.exampleWsdefdef(essential,
						wsInfo.wsdefdefPackage(),
						wsInfo.wsdefdefClassSimpleName(), wsInfo.wsName(),
						wsDefPackage + "." + wsDefName));
		File wsDefJava = new File(iHave,
				"/wsdef/src/main/java" + "/" + wsDefPackage.replace(".", "/")
						+ "/" + wsDefName + "Factory.java");
		FileUtil.textFileEnsuredToHaveContent(wsDefJava, ExampleWsDefGenerator
				.exampleWsdef(essential, wsDefPackage, wsDefName));
		File wsJava = new File(iHave, "/wsdef/src/main/java" + "/"
				+ wsDefPackage.replace(".", "/") + "/" + wsDefName + ".java");
		FileUtil.textFileEnsuredToHaveContent(wsJava, ExampleWsDefGenerator
				.exampleWs(essential, wsDefPackage, wsDefName));
		// TODO it's a bit ugly to create dummy target and side-effect just to
		// get proper names for wish scripts:
		HelloSideEffect stubEclipseSettingsSe = new HelloSideEffect(
				"eclipse-settings");
		refreshWishScripts(asSomeone,
				Arrays.asList(new HelloTarget("hello", "not needed")),
				Arrays.asList(stubEclipseSettingsSe));
		IwantException e = new IwantException("I created\n"
				+ wsInfo.wsdefdefJava() + "\nand\n" + wsDefJava + "\nand\n"
				+ wsJava
				+ "\nPlease edit them and rerun me.\nIf you want to use Eclipse for editing, run "
				+ new File(withBashIwantFile(asSomeone),
						toWish(stubEclipseSettingsSe))
				+ " first.");
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

	private static void refreshWishScripts(File asSomeone, Workspace wsDef,
			TargetDefinitionContext tdCtx, SideEffectDefinitionContext sedCtx) {
		refreshWishScripts(asSomeone, wsDef.targets(tdCtx),
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

	private static List<File> iwantApiClassLocations()
			throws URISyntaxException {
		SortedSet<File> apiClassLocations = new TreeSet<>();
		apiClassLocations.add(classesDirOf(
				"/org/fluentjava/iwant/" + "api/antrunner/AntRunner.class"));
		apiClassLocations.add(classesDirOf("/org/fluentjava/iwant/"
				+ "api/bash/TargetImplementedInBash.class"));
		apiClassLocations.add(classesDirOf(
				"/org/fluentjava/iwant/" + "api/core/HelloTarget.class"));
		apiClassLocations.add(classesDirOf(
				"/org/fluentjava/iwant/" + "api/javamodules/JavaModule.class"));
		apiClassLocations.add(classesDirOf(
				"/org/fluentjava/iwant/" + "api/model/Path.class"));
		apiClassLocations.add(classesDirOf(
				"/org/fluentjava/iwant/" + "api/wsdef/Workspace.class"));
		// TODO maybe make eclipse-settings an optional plugin
		apiClassLocations.add(classesDirOf("/org/fluentjava/iwant/"
				+ "eclipsesettings/EclipseSettings.class"));
		apiClassLocations.add(classesDirOf(
				"/org/fluentjava/iwant/" + "api/target/TargetBase.class"));
		apiClassLocations.add(classesDirOf("/org/fluentjava/iwant/"
				+ "core/javafinder/WsdefJavaOf.class"));
		return new ArrayList<>(apiClassLocations);
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
		throw new IwantException(
				"I created " + wsInfoFile + "\nPlease edit it and rerun me.");
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
		FileUtil.textFileEnsuredToHaveContent(wsInfo,
				"# paths are relative to this file's directory\n"
						+ "WSNAME=example\n" + "WSROOT=../../..\n"
						+ "WSDEFDEF_MODULE=../wsdefdef\n"
						+ "WSDEFDEF_CLASS=com.example.wsdefdef.ExampleWorkspaceProvider\n");
	}

	private static void createScript(File file, String content) {
		FileUtil.textFileEnsuredToHaveContent(file, content);
		file.setExecutable(true);
	}

}
