package net.sf.iwant.plugin.findbugs;

import java.io.File;

import net.sf.iwant.api.core.TargetBase;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.core.download.Downloaded;
import net.sf.iwant.plugin.ant.Untarred;

public class FindbugsDistribution extends TargetBase {

	private final String version;
	private final Downloaded tarGz;

	public FindbugsDistribution(String version) {
		super("findbugs-" + version);
		this.version = version;
		this.tarGz = tarGz(version);
	}

	public static FindbugsDistribution ofVersion(String version) {
		return new FindbugsDistribution(version);
	}

	private static Downloaded tarGz(String version) {
		String tarGzName = "findbugs-" + version + ".tar.gz";
		return Downloaded
				.withName(tarGzName)
				.url("http://downloads.sourceforge.net/project/findbugs/findbugs/"
						+ version + "/" + tarGzName).noCheck();
	}

	public Downloaded tarGz() {
		return tarGz;
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("tarGz", tarGz).nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);

		File cachedTarGz = ctx.cached(tarGz);

		Untarred.untarTo(cachedTarGz, dest, "gzip");
	}

	public File homeDirectory(TargetEvaluationContext ctx) {
		return new File(ctx.cached(this), "findbugs-" + version);
	}

}
