package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.entry2.Iwant2;

public class JavaClasses extends Target {

	private final List<Path> ingredients;
	private final Path srcDir;

	public JavaClasses(String name, Path srcDir) {
		super(name);
		this.srcDir = srcDir;
		this.ingredients = Arrays.asList(srcDir);
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.freshPathTo(this);
		List<File> javaFiles = Iwant2.javaFilesUnder(ctx.freshPathTo(srcDir));
		List<File> classLocations = Collections.emptyList();
		ctx.iwant().compiledClasses(dest, javaFiles, classLocations);
	}

	@Override
	public List<Path> ingredients() {
		return ingredients;
	}

}
