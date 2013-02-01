package net.sf.iwant.testing;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.Iwant.UnmodifiableSource;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.entry.Iwant.UnmodifiableZip;
import net.sf.iwant.testarea.TestArea;

public class IwantNetworkMock implements IwantNetwork {

	private final TestArea testArea;
	private Map<UnmodifiableSource<?>, File> cachedUnmodifiables = new HashMap<UnmodifiableSource<?>, File>();
	private URL junitUrl;
	private URL svnkitUrl;

	public IwantNetworkMock(TestArea testArea) {
		this.testArea = testArea;
	}

	private <T> T nonNull(T value, Object request) {
		if (value == null) {
			throw new IllegalStateException("You forgot to teach " + request
					+ "\nto " + this);
		}
		return value;
	}

	@Override
	public File cacheLocation(UnmodifiableSource<?> src) {
		return nonNull(cachedUnmodifiables.get(src), src);
	}

	public File cachesAt(UnmodifiableSource<?> src, File cached) {
		cachedUnmodifiables.put(src, cached);
		return cached;
	}

	public File cachesAt(UnmodifiableSource<?> src, String pathInTestArea) {
		return cachesAt(src, new File(testArea.root(), pathInTestArea));
	}

	public void cachesUrlAt(String url, String pathInTestArea) {
		try {
			cachesUrlAt(new URL(url), pathInTestArea);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public File cachesUrlAt(URL url, String pathInTestArea) {
		return cachesAt(new UnmodifiableUrl(url), pathInTestArea);
	}

	public File cachesZipAt(URL url, String pathInTestArea) {
		return cachesAt(new UnmodifiableZip(url), pathInTestArea);
	}

	@Override
	public URL svnkitUrl() {
		return nonNull(svnkitUrl, "svnkitUrl");
	}

	public void hasSvnkitUrl(URL svnkitUrl) {
		this.svnkitUrl = svnkitUrl;
	}

	public void hasSvnkitUrl(String svnkitUrl) {
		try {
			hasSvnkitUrl(new URL(svnkitUrl));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public URL junitUrl() {
		return nonNull(junitUrl, "junitUrl");
	}

	public void hasJunitUrl(URL junitUrl) {
		this.junitUrl = junitUrl;
	}

	public void usesRealJunitUrlAndCached() {
		// assuming real download works we ensure real junit is cached in real
		// cache:
		Iwant iwant = Iwant.usingRealNetwork();
		URL realJunitUrl = iwant.network().junitUrl();
		hasJunitUrl(realJunitUrl);
		cachesAt(new UnmodifiableUrl(realJunitUrl),
				iwant.downloaded(realJunitUrl));
	}

	public void usesRealSvnkitUrlAndCacheAndUnzipped() {
		// here we assume real download has been tested
		Iwant iwant = Iwant.usingRealNetwork();
		URL realUrl = iwant.network().svnkitUrl();
		hasSvnkitUrl(realUrl);

		UnmodifiableUrl realUrlSrc = new UnmodifiableUrl(realUrl);
		File downloaded = iwant.network().cacheLocation(realUrlSrc);
		cachesAt(realUrlSrc, downloaded);

		UnmodifiableZip zip = new UnmodifiableZip(Iwant.fileToUrl(downloaded));
		cachesAt(zip, iwant.unmodifiableZipUnzipped(zip));
	}

	@Override
	public String toString() {
		return "IwantNetworkMock [testArea=" + testArea
				+ ", cachedUnmodifiables=" + cachedUnmodifiables
				+ ", junitUrl=" + junitUrl + ", svnkitUrl=" + svnkitUrl + "]";
	}

}
