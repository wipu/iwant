package net.sf.iwant.core.download;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.iwant.api.core.TargetBase;
import net.sf.iwant.api.model.CacheScopeChoices;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.entry.Iwant;

public class Downloaded extends TargetBase {

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
		private URL url;

		public DownloadedSpex(String name) {
			this.name = name;
		}

		public DownloadedSpex url(String url) {
			return url(Iwant.url(url));
		}

		public DownloadedSpex url(URL url) {
			this.url = url;
			return this;
		}

		public Downloaded md5(String md5) {
			return new Downloaded(name, url, md5);
		}

		public Downloaded noCheck() {
			return new Downloaded(name, url, null);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		// TODO tee to both cached file and out from here
		throw new UnsupportedOperationException("TODO test and implement");
	}

	/**
	 * Most repositories limit the number of concurrent downloads so this is the
	 * simplest way to avoid failed downloads because of server refusing the
	 * connection.
	 */
	@Override
	public boolean supportsParallelism() {
		return false;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		ctx.iwant().downloaded(url, dest);

		if (md5 != null) {
			byte[] fileContent = FileUtil.contentAsBytes(dest);
			String actualMd5 = md5(fileContent);
			if (!md5.equals(actualMd5)) {
				File corruptedFile = new File(dest.getCanonicalPath()
						+ ".corrupted");
				dest.renameTo(corruptedFile);
				Iwant.debugLog("Downloaded", "checksum failed " + dest);
				throw new Iwant.IwantException("Actual MD5 was " + actualMd5
						+ ", moved downloaded file to " + corruptedFile);
			}
		}
	}

	private static String md5(byte[] bytes) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(bytes);
		byte[] digest = md.digest();
		return asHex(digest);
	}

	private static String asHex(byte[] in) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < in.length; i++) {
			int shaByte = in[i] & 0xFF;
			if (shaByte < 0x10) {
				out.append('0');
			}
			out.append(Integer.toHexString(shaByte));
		}
		return out.toString();
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
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.parameter("url", url).parameter("md5", md5).nothingElse();
	}

	public URL url() {
		return url;
	}

	public String md5() {
		return md5;
	}

}
