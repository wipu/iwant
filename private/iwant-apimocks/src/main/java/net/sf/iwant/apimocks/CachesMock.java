package net.sf.iwant.apimocks;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.iwant.api.model.CacheScopeChoices;
import net.sf.iwant.api.model.Caches;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;

public class CachesMock implements Caches {

	private final File wsRoot;
	private File cachedModifiableTargets;
	private File cachedDescriptors;
	private Map<UrlString, File> cachedUrls = new HashMap<>();
	private File temporaryDirectory;

	public CachesMock(File wsRoot) {
		this.wsRoot = wsRoot;
	}

	@Override
	public File contentOf(Path path) {
		return path.cachedAt(new ContentChoices());
	}

	private class ContentChoices implements CacheScopeChoices {

		@Override
		public File target(Target target) {
			return new File(cachedModifiableTargets(), target.name());
		}

		@Override
		public File source(Source target) {
			return new File(wsRoot, target.name());
		}

		@Override
		public File unmodifiableUrl(URL url) {
			return nonNull(cachedUrls.get(new UrlString(url)), "cache for url "
					+ url);
		}

	}

	@Override
	public File contentDescriptorOf(Target target) {
		return new File(cachedDescriptors(), target.name());
	}

	public void cachesModifiableTargetsAt(File cachedModifiableTargets) {
		this.cachedModifiableTargets = cachedModifiableTargets;
	}

	public void cachesUrlAt(URL url, File cached) {
		cachedUrls.put(new UrlString(url), cached);
	}

	public void cachesDesciptorsAt(File cachedDescriptors) {
		this.cachedDescriptors = cachedDescriptors;
	}

	private File cachedModifiableTargets() {
		return nonNull(cachedModifiableTargets, "cachedModifiableTargets");
	}

	private File cachedDescriptors() {
		return nonNull(cachedDescriptors, "cachedDescriptorsDir");
	}

	@Override
	public File temporaryDirectory(String workerName) {
		return nonNull(temporaryDirectory, "temporaryDirectory");
	}

	public void providesTemporaryDirectoryAt(File temporaryDirectory) {
		this.temporaryDirectory = temporaryDirectory;
	}

	private <T> T nonNull(T value, Object request) {
		if (value == null) {
			throw new IllegalStateException("You forgot to teach " + request
					+ "\nto " + this);
		}
		return value;
	}

}
