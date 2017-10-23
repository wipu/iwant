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
	private boolean shallNotFindSystemJavaCompiler;

	public IwantNetworkMock(TestArea testArea) {
		this.testArea = testArea;
	}

	@Override
	public File cacheLocation(UnmodifiableSource<?> src) {
		return NullCheck.nonNull(cachedUnmodifiables.get(src), src);
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

	public File cachesUrlAt(URL url, File cached) {
		return cachesAt(new UnmodifiableUrl(url), cached);
	}

	public File cachesZipAt(URL url, String pathInTestArea) {
		return cachesAt(new UnmodifiableZip(url), pathInTestArea);
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

	public void usesRealCacheFor(URL url) {
		UnmodifiableUrl src = new UnmodifiableUrl(url);
		cachesAt(src, Iwant.usingRealNetwork().network().cacheLocation(src));
	}

}