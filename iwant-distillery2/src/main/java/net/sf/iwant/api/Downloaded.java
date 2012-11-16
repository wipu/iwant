package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.entry.Iwant;

public class Downloaded extends Target {

	private final URL url;
	private final String md5;

	private Downloaded(String name, URL url, String md5) {
		super(name);
		this.url = url;
		this.md5 = md5;
	}

	public static DownloadedSpex withName(String name) {
		return new DownloadedSpex(name);
	}

	public static class DownloadedSpex {

		private final String name;
		private String url;

		public DownloadedSpex(String name) {
			this.name = name;
		}

		public DownloadedSpex url(String url) {
			this.url = url;
			return this;
		}

		public Downloaded md5(String md5) {
			return new Downloaded(name, Iwant.url(url), md5);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		// TODO tee to both cached file and out from here
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		// TODO pass md5 for verification
		ctx.iwant().downloaded(url, ctx.cached(this));
	}

	@Override
	public File cachedAt(CacheScopeChoices cachedAt) {
		return cachedAt.unmodifiableUrl(url);
	}

	/**
	 * When the workspace-specific content descriptor is missing, we are dirty
	 * so a download is retried. But we don't want the globally cached (in the
	 * user's home directory) downloaded file to be deleted. This way the
	 * download does nothing if the cached file exists, except caches the
	 * content descriptor.
	 */
	@Override
	public boolean expectsCachedTargetMissingBeforeRefresh() {
		return false;
	}

	@Override
	public List<Path> ingredients() {
		return Collections.emptyList();
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");
		b.append("  url:" + url).append("\n");
		b.append("}\n");
		return b.toString();
	}

	public URL url() {
		return url;
	}

	public String md5() {
		return md5;
	}

}
