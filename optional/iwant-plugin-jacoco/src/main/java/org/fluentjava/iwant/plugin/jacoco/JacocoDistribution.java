package org.fluentjava.iwant.plugin.jacoco;

import java.io.File;
import java.io.InputStream;

import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.api.zip.Unzipped;
import org.fluentjava.iwant.core.download.Downloaded;

public class JacocoDistribution extends TargetBase {

	private final Downloaded zip;

	private JacocoDistribution(String version) {
		super("jacoco-" + version);
		this.zip = zip(version);
	}

	public static JacocoDistribution ofVersion(String version) {
		return new JacocoDistribution(version);
	}

	public static JacocoDistribution newestTestedVersion() {
		return ofVersion("0.8.2");
	}

	private static Downloaded zip(String version) {
		String zipName = "jacoco-" + version + ".zip";
		return Downloaded.withName(zipName)
				.url("http://repo1.maven.org/maven2/org/jacoco/jacoco/"
						+ version + "/" + zipName)
				.noCheck();
	}

	public Downloaded zip() {
		return zip;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("zip", zip).nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		Unzipped.unzipTo(ctx.cached(zip), ctx.cached(this));
	}

	public File jacocoagentJar(TargetEvaluationContext ctx) {
		return new File(ctx.cached(this), "lib/jacocoagent.jar");
	}

	public File jacocoantJar(TargetEvaluationContext ctx) {
		return new File(ctx.cached(this), "lib/jacocoant.jar");
	}

}
