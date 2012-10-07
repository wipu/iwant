package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;

public class Downloaded extends Target {

	private final URL url;

	private Downloaded(String name, URL url) {
		super(name);
		this.url = url;
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

		public Downloaded md5(@SuppressWarnings("unused") String md5) {
			// TODO pass md5 for verification
			return new Downloaded(name, Iwant.url(url));
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		// TODO tee to both cached file and out from here
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		ctx.iwant().downloaded(url);
	}

	@Override
	public File cachedAt(TargetEvaluationContext ctx) {
		UnmodifiableUrl src = new UnmodifiableUrl(url);
		return ctx.iwant().network().cacheLocation(src);
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

}
