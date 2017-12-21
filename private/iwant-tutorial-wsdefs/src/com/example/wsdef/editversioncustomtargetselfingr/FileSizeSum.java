package com.example.wsdef.editversioncustomtargetselfingr;

import java.io.File;
import java.util.List;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.core.javafinder.WsdefJavaOf;
import org.fluentjava.iwant.coreservices.FileUtil;

class FileSizeSum extends TargetBase {

	private final List<Path> pathsToSum;
	private final String headerLineContent;
	private final Source me;

	public FileSizeSum(String name, List<Path> pathsToSum,
			String headerLineContent, WsdefJavaOf wsdefJavaOf) {
		super(name);
		this.pathsToSum = pathsToSum;
		this.headerLineContent = headerLineContent;
		this.me = wsdefJavaOf.classUnderSrcMainJava(getClass());
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("pathsToSum", pathsToSum)
				.parameter("headerLineContent", headerLineContent)
				.ingredients("me", me).nothingElse();
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
