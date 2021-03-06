package com.example.wsdef.editversioncustomtargetdeclaredingr;

import java.io.File;
import java.util.List;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.coreservices.FileUtil;

class FileSizeSum extends TargetBase {

	private final List<Path> pathsToSum;

	public FileSizeSum(String name, List<Path> pathsToSum) {
		super(name);
		this.pathsToSum = pathsToSum;
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("pathsToSum", pathsToSum).nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		System.err.println("Refreshing " + dest);

		int pathSizeSum = 0;
		for (Path path : pathsToSum) {
			File pathFile = ctx.cached(path);
			int pathSize = FileUtil.contentAsBytes(pathFile).length;
			pathSizeSum += pathSize;
		}
		FileUtil.newTextFile(dest, pathSizeSum + "\n");
	}

}
