package com.example.wsdef.editversioncustomtargetselfingr;

import java.io.File;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.api.target.TargetBase;
import net.sf.iwant.coreservices.FileUtil;

class FileSizeSum extends TargetBase {

	private final List<Path> pathsToSum;
	private final String headerLineContent;

	public FileSizeSum(String name, List<Path> pathsToSum,
			String headerLineContent) {
		super(name);
		this.pathsToSum = pathsToSum;
		this.headerLineContent = headerLineContent;
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("pathsToSum", pathsToSum)
				.parameter("headerLineContent", headerLineContent)
				.ingredients("me",
						Source.underWsroot(
								"as-iwant-tutorial-developer/i-have/wsdef/"
										+ "src/main/java/"
										+ "com/example/wsdef/FileSizeSum.java"))
				.nothingElse();
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
		FileUtil.newTextFile(dest,
				headerLineContent + "\n" + pathSizeSum + "\n");
	}

}
