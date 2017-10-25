package org.fluentjava.iwant.entrymocks;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.tools.JavaCompiler;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantNetwork;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableSource;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableUrl;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableZip;
import org.fluentjava.iwant.testarea.TestArea;

public class IwantNetworkMock implements IwantNetwork {

	private final TestArea testArea;
	private Map<UnmodifiableSource<?>, File> cachedUnmodifiables = new HashMap<>();
	private boolean shallNotFindSystemJavaCompiler;

	public IwantNetworkMock(TestArea testArea) {
		this.testArea = testArea;
	}

	@Override
	public File cacheOfContentFrom(UnmodifiableSource<?> src) {
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
		cachesAt(src,
				Iwant.usingRealNetwork().network().cacheOfContentFrom(src));
	}

}