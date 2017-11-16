package net.sf.iwant.wsdef;

import java.io.File;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.coreservices.FileUtil;

public class ExtendedIwantEnums extends TargetBase {

	private final EnumSrc codeStyleJava = new EnumSrc(
			"essential/iwant-api-javamodules/src/main/java",
			"org/fluentjava/iwant/api/javamodules", "CodeStyle.java");
	private final EnumSrc codeStyleValueJava = new EnumSrc(
			"essential/iwant-api-javamodules/src/main/java",
			"org/fluentjava/iwant/api/javamodules", "CodeStyleValue.java");

	public ExtendedIwantEnums(String name) {
		super(name);
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("codeStyleJava", codeStyleJava.source())
				.ingredients("codeStyleValueJava", codeStyleValueJava.source())
				.nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		extend(ctx, codeStyleJava);
		extend(ctx, codeStyleValueJava);
	}

	private static void extend(TargetEvaluationContext ctx, EnumSrc enumSrc) {
		String content = FileUtil.contentAsString(ctx.cached(enumSrc.source()));
		content = content.replaceFirst("([A-Z_]*,)", "_ILLEGAL_, $1");
		FileUtil.newTextFile(enumSrc.destination(ctx), content);
	}

	private class EnumSrc {

		private String srcDir;
		private String package_;
		private String fileName;

		EnumSrc(String srcDir, String package_, String fileName) {
			this.srcDir = srcDir;
			this.package_ = package_;
			this.fileName = fileName;
		}

		Path source() {
			return Source.underWsroot(srcDir + "/" + package_ + "/" + fileName);
		}

		File destination(TargetEvaluationContext ctx) {
			return new File(ctx.cached(ExtendedIwantEnums.this),
					package_ + "/" + fileName);
		}

	}

}
