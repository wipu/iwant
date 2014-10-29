package net.sf.iwant.entrymocks;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.tools.JavaCompiler;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.Iwant.UnmodifiableSource;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.entry.Iwant.UnmodifiableZip;
import net.sf.iwant.testarea.TestArea;

public class IwantNetworkMock implements IwantNetwork {

	private final TestArea testArea;
	private Map<UnmodifiableSource<?>, File> cachedUnmodifiables = new HashMap<>();
	private URL svnkitUrl;
	private boolean shallNotFindSystemJavaCompiler;

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
		hasSvnkitUrl(Iwant.url(svnkitUrl));
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
	public JavaCompiler systemJavaCompiler() {
		if (shallNotFindSystemJavaCompiler) {
			return null;
		}
		return Iwant.usingRealNetwork().network().systemJavaCompiler();
	}

	public void shallNotFindSystemJavaCompiler() {
		this.shallNotFindSystemJavaCompiler = true;
	}

}
