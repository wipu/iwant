package net.sf.iwant.core.download;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.api.model.CacheScopeChoices;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.coreservices.FileUtil;

public class SvnExported extends Target {

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
	public List<Path> ingredients() {
		return Collections.emptyList();
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
		dest.mkdirs();
		FileUtil.copyRecursively(tmpExported, dest, true);
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + url;
	}

	public URL url() {
		return url;
	}

}
