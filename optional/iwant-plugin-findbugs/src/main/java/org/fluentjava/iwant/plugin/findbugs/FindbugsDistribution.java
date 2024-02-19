package org.fluentjava.iwant.plugin.findbugs;

import java.io.File;

import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.plugin.ant.Untarred;

public class FindbugsDistribution extends TargetBase {

	private final String version;
	private final Downloaded tarGz;

	public static FindbugsDistribution _4_7_3 = new FindbugsDistribution(
			"4.7.3", "9739f70965c4b89a365419af9c688934");

	public static FindbugsDistribution _4_8_3 = new FindbugsDistribution(
			"4.8.3", "dae83e21c1c3014ea177c882bb72c1e9");

	public FindbugsDistribution(String version, String md5sum) {
		super("spotbugs-" + version);
		this.version = version;
		this.tarGz = tarGz(version, md5sum);
	}

	public static FindbugsDistribution ofVersion(String version,
			String md5sum) {
		return new FindbugsDistribution(version, md5sum);
	}

	private static Downloaded tarGz(String version, String md5sum) {
		String tarGzName = "spotbugs-" + version + ".tgz";
		return Downloaded.withName(tarGzName)
				.url("https://github.com/spotbugs/spotbugs/releases/download/"
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
		return new File(ctx.cached(this), "spotbugs-" + version);
	}

}
