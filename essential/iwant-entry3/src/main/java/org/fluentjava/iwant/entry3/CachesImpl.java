package net.sf.iwant.entry3;

import java.io.File;
import java.net.URL;

import net.sf.iwant.api.model.CacheScopeChoices;
import net.sf.iwant.api.model.Caches;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;

public class CachesImpl implements Caches {

	private final File wsCache;
	private final File wsRoot;
	private final IwantNetwork network;

	public CachesImpl(File wsCache, File wsRoot, IwantNetwork network) {
		this.wsCache = wsCache;
		this.wsRoot = wsRoot;
		this.network = network;
	}

	@Override
	public File contentOf(Path path) {
		return path.cachedAt(new ContentChoices());
	}

	private class ContentChoices implements CacheScopeChoices {

		@Override
		public File target(Target target) {
			return new File(wsCache, "target/" + target.name());
		}

		@Override
		public File source(Source target) {
			return new File(wsRoot, target.name());
		}

		@Override
		public File unmodifiableUrl(URL url) {
			UnmodifiableUrl src = new UnmodifiableUrl(url);
			return network.cacheOfContentFrom(src);
		}

	}

	@Override
	public File contentDescriptorOf(Target target) {
		return new File(wsCache, "descriptor/" + target.name());
	}

	@Override
	public File temporaryDirectory(String workerName) {
		File parent = new File(wsCache, "temp");
		File tmpDir = new File(parent, workerName);
		Iwant.del(tmpDir);
		Iwant.mkdirs(tmpDir);
		return tmpDir;
	}

}
