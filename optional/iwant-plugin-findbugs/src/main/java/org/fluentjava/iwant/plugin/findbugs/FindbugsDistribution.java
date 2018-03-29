package org.fluentjava.iwant.plugin.findbugs;

import java.io.File;

import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.plugin.ant.Untarred;

public class FindbugsDistribution extends TargetBase {

	private final String version;
	private final Downloaded tarGz;

	public static FindbugsDistribution _3_0_0 = new FindbugsDistribution(
			"3.0.0", "f0915a0800a926961296da28b6ada7cc");
	public static FindbugsDistribution _3_0_1 = new FindbugsDistribution(
			"3.0.1", "dec8828de8657910fcb258ce5383c168");

	public FindbugsDistribution(String version, String md5sum) {
		super("findbugs-" + version);
		this.version = version;
		this.tarGz = tarGz(version, md5sum);
	}

	public static FindbugsDistribution ofVersion(String version,
			String md5sum) {
		return new FindbugsDistribution(version, md5sum);
	}

	private static Downloaded tarGz(String version, String md5sum) {
		String tarGzName = "findbugs-" + version + ".tar.gz";
		return Downloaded.withName(tarGzName)
				.url("http://downloads.sourceforge.net/project/findbugs/findbugs/"
						+ version + "/" + tarGzName)
				.md5(md5sum);
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
