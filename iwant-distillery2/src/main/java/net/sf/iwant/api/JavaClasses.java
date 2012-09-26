package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.entry2.Iwant2;

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

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.freshPathTo(this);
		List<File> javaFiles = Iwant2.javaFilesUnder(ctx.freshPathTo(srcDir));
		List<File> classLocationDirs = new ArrayList<File>();
		for (Path classLocation : classLocations) {
			File classLocationDir = classLocation.cachedAt(ctx.cached());
			classLocationDirs.add(classLocationDir);
		}
		ctx.iwant().compiledClasses(dest, javaFiles, classLocationDirs);
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
