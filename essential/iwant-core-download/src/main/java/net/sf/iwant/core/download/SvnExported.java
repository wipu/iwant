package net.sf.iwant.core.download;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import net.sf.iwant.api.model.CacheScopeChoices;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.api.target.TargetBase;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.entry.Iwant;

public class SvnExported extends TargetBase {

	private final URL url;

	private SvnExported(String name, URL url) {
		super(name);
		this.url = url;
	}

	public static SvnExportedPlease with() {
		return new SvnExportedPlease();
	}

	public static class SvnExportedPlease {

		private String name;
		private URL url;

		public SvnExported end() {
			return new SvnExported(name, url);
		}

		public SvnExportedPlease name(String name) {
			this.name = name;
			return this;
		}

		public SvnExportedPlease url(URL url) {
			this.url = url;
			return this;
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.parameter("url", url).nothingElse();
	}

	/**
	 * Let's behave towards the sf.net svn server
	 */
	@Override
	public boolean supportsParallelism() {
		return false;
	}

	@Override
	public File cachedAt(CacheScopeChoices cachedAt) {
		return cachedAt.unmodifiableUrl(url);
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File tmp = ctx.freshTemporaryDirectory();
		File tmpExported = new File(tmp, "exported");
		ctx.iwant().svnExported(url, tmpExported);

		File dest = ctx.cached(this);
		Iwant.mkdirs(dest);
		FileUtil.copyRecursively(tmpExported, dest, true);
	}

	public URL url() {
		return url;
	}

}
