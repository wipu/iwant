package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class JavaClasses extends Target {

	private final List<Path> ingredients;
	private final Path srcDir;
	private final Collection<? extends Path> classLocations;

	public JavaClasses(String name, Path srcDir,
			Collection<? extends Path> classLocations) {
		super(name);
		this.srcDir = srcDir;
		this.classLocations = classLocations;
		this.ingredients = new ArrayList<Path>();
		this.ingredients.add(srcDir);
		for (Path classLocation : classLocations) {
			this.ingredients.add(classLocation);
		}
	}

	public Collection<? extends Path> classLocations() {
		return classLocations;
	}

	public Path srcDir() {
		return srcDir;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.freshPathTo(this);
		List<File> javaFiles = javaFilesUnder(ctx.freshPathTo(srcDir));
		List<File> classLocationDirs = new ArrayList<File>();
		for (Path classLocation : classLocations) {
			File classLocationDir = classLocation.cachedAt(ctx);
			classLocationDirs.add(classLocationDir);
		}
		ctx.iwant().compiledClasses(dest, javaFiles, classLocationDirs);
	}

	private static List<File> javaFilesUnder(File dir) {
		List<File> srcFiles = new ArrayList<File>();
		File[] files = dir.listFiles();
		Arrays.sort(files);
		for (File file : files) {
			if (file.isDirectory()) {
				srcFiles.addAll(javaFilesUnder(file));
			} else {
				if (file.getAbsolutePath().endsWith(".java")) {
					srcFiles.add(file);
				}
			}
		}
		return srcFiles;
	}

	@Override
	public List<Path> ingredients() {
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");
		b.append("  src:").append(srcDir).append("\n");
		for (Path classLocation : classLocations) {
			b.append("  classes:").append(classLocation).append("\n");
		}
		b.append("}");
		return b.toString();
	}

}
