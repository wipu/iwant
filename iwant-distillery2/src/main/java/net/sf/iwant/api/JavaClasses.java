package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.entry2.Iwant2;

public class JavaClasses implements Target {

	private final String name;
	private final List<Target> ingredients;
	private final Target srcDir;

	public JavaClasses(String name, Target srcDir) {
		this.name = name;
		this.srcDir = srcDir;
		this.ingredients = Arrays.asList(srcDir);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public File path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.freshPathTo(this);
		List<File> javaFiles = Iwant2.javaFilesUnder(ctx.freshPathTo(srcDir));
		List<File> classLocations = Collections.emptyList();
		return ctx.iwant().compiledClasses(dest, javaFiles, classLocations);
	}

	@Override
	public List<Target> ingredients() {
		return ingredients;
	}

}
