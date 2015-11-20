package net.sf.iwant.wsdef;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

public class CopyOfLocalIwantWsForTutorial extends Target {

	private final List<Path> ingredients = new ArrayList<>();

	public CopyOfLocalIwantWsForTutorial() {
		super("copy-of-local-iwant-ws-for-tutorial");
		try {
			addIngredients();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * TODO reuse from somewhere
	 */
	private File findWsRoot() throws URISyntaxException {
		File candidate = new File(getClass()
				.getResource(getClass().getSimpleName() + ".class").toURI());
		while (candidate.getParentFile() != null) {
			if (new File(candidate,
					"essential/iwant-wsroot-marker/iwant-wsroot-marker.txt")
							.exists()) {
				return candidate;
			}
			candidate = candidate.getParentFile();
		}
		throw new IllegalStateException("Cannot find wsRoot");
	}

	/**
	 * no iwant-docs here, otherwise editing them triggers new copy and the
	 * whole optimization breaks
	 */
	private void addIngredients() throws URISyntaxException {
		File wsRoot = findWsRoot();
		// no iwant-docs here, otherwise editing them triggers new copy and the
		// whole optimization breaks
		ingredients.add(Source
				.underWsroot("essential/iwant-entry/as-some-developer/with"));
		ingredients
				.addAll(mainJavasOfModulesUnder(new File(wsRoot, "essential")));
		ingredients
				.addAll(mainJavasOfModulesUnder(new File(wsRoot, "optional")));
		ingredients.add(Source.underWsroot("as-iwant-developer/i-have/wsdef/"
				+ "src/main/java/net/sf/iwant/wsdef/CopyOfLocalIwantWsForTutorial.java"));
	}

	private static List<Path> mainJavasOfModulesUnder(File modulesDir) {
		List<Path> mainJavas = new ArrayList<>();
		File[] potentialMod = modulesDir.listFiles();
		Arrays.sort(potentialMod);
		for (File mod : potentialMod) {
			File mainJava = new File(mod, "src/main/java");
			if (mainJava.exists()) {
				mainJavas.add(Source.underWsroot(modulesDir.getName() + "/"
						+ mod.getName() + "/src/main/java"));
			}
		}
		return mainJavas;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

	private static List<String> nonDirFilesToCopy(File dir, String dirName) {
		System.err.println("Finding files to copy from " + dir);
		List<String> files = new ArrayList<>();
		for (File file : dir.listFiles()) {
			String fileName = dirName + "/" + file.getName();
			if (!mustCopy(fileName)) {
				continue;
			}
			if (file.isDirectory()) {
				files.addAll(nonDirFilesToCopy(file, fileName));
			} else {
				files.add(fileName);
			}
		}
		return files;
	}

	private static boolean mustCopy(String fileName) {
		if (fileName.startsWith("/as-iwant-developer")) {
			return false;
		}
		if (fileName.startsWith("/private")) {
			return false;
		}
		if (fileName.endsWith("/.project")) {
			return false;
		}
		if (fileName.endsWith("/.classpath")) {
			return false;
		}
		if (fileName.endsWith("/.settings")) {
			return false;
		}
		if (fileName.endsWith("/classes")) {
			return false;
		}
		return true;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		List<String> filesToCopy = nonDirFilesToCopy(ctx.wsRoot(), "");
		System.err.println("Copying");
		for (String f : filesToCopy) {
			File from = new File(ctx.wsRoot(), f);
			File to = new File(dest, f);
			FileUtils.copyFile(from, to);
		}
		System.err.println("Fixing permissions");
		new File(dest,
				"essential/iwant-entry/as-some-developer/with/bash/iwant/help.sh")
						.setExecutable(true);
	}

}
