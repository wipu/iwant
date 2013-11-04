package net.sf.iwant.plugin.findbugs;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.api.Downloaded;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.plugin.ant.Untarred;

public class FindbugsDistribution extends Target {

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
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.add(tarGz);
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
		File dest = ctx.cached(this);

		File cachedTarGz = ctx.cached(tarGz);

		Untarred.untarTo(cachedTarGz, dest, "gzip");
	}

	public File homeDirectory(TargetEvaluationContext ctx) {
		return new File(ctx.cached(this), "findbugs-" + version);
	}

}
