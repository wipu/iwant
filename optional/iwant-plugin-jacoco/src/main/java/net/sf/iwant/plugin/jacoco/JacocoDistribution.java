package net.sf.iwant.plugin.jacoco;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.core.download.Downloaded;
import net.sf.iwant.plugin.ant.Unzipped;

public class JacocoDistribution extends Target {

	private final String version;
	private final Downloaded zip;

	private JacocoDistribution(String version) {
		super("jacoco-" + version);
		this.version = version;
		this.zip = zip(version);
	}

	public static JacocoDistribution ofVersion(String version) {
		return new JacocoDistribution(version);
	}

	public static JacocoDistribution newestTestedVersion() {
		return ofVersion("0.7.2.201409121644");
	}

	private static Downloaded zip(String version) {
		String zipName = "jacoco-" + version + ".zip";
		return Downloaded
				.withName(zipName)
				.url("http://repo1.maven.org/maven2/org/jacoco/jacoco/"
						+ version + "/" + zipName).noCheck();
	}

	public Downloaded zip() {
		return zip;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<>();
		ingredients.add(zip);
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");
		for (Path ingredient : ingredients()) {
			b.append("  ").append(ingredient).append("\n");
		}
		b.append("}\n");
		return b.toString();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		Unzipped.unzipTo(ctx.cached(zip), ctx.cached(this));
	}

	public File jacocoagentJar(TargetEvaluationContext ctx) {
		return new File(ctx.cached(this), "lib/jacocoagent.jar");
	}

	public File orgJacocoAntJar(TargetEvaluationContext ctx) {
		return new File(ctx.cached(this), "lib/org.jacoco.ant-" + version
				+ ".jar");
	}

	public File orgJacocoCoreJar(TargetEvaluationContext ctx) {
		return new File(ctx.cached(this), "lib/org.jacoco.core-" + version
				+ ".jar");
	}

	public File orgJacocoReportJar(TargetEvaluationContext ctx) {
		return new File(ctx.cached(this), "lib/org.jacoco.report-" + version
				+ ".jar");
	}

}
