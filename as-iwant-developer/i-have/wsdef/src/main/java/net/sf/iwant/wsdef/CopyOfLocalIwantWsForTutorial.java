package net.sf.iwant.wsdef;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

import org.apache.commons.io.FileUtils;

public class CopyOfLocalIwantWsForTutorial extends Target {

	public CopyOfLocalIwantWsForTutorial() {
		super("copy-of-local-iwant-ws-for-tutorial");
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.addAll(relevantModuleDirPaths());
		ingredients
				.add(Source
						.underWsroot("as-iwant-developer/i-have/wsdef/"
								+ "src/main/java/net/sf/iwant/wsdef/CopyOfLocalIwantWsForTutorial.java"));
		return ingredients;
	}

	private static List<Path> relevantModuleDirPaths() {
		List<Path> ingredients = new ArrayList<Path>();
		for (String srcName : relevantSourceNames()) {
			ingredients.add(Source.underWsroot(srcName));
		}
		return ingredients;
	}

	private static List<String> relevantSourceNames() {
		// no iwant-docs here, otherwise editing them triggers new copy and the
		// whole optimization breaks
		return Arrays.asList("essential", "optional");
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

	private static List<String> nonDirFilesToCopy(File dir, String dirName) {
		System.err.println("Finding files to copy from " + dir);
		List<String> files = new ArrayList<String>();
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
		if ("/as-iwant-developer/.i-cached".equals(fileName)) {
			return false;
		}
		if ("/private/iwant-testarea/testarea-root".equals(fileName)) {
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
