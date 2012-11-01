package net.sf.iwant.entry3;

import java.io.File;
import java.net.URL;

import net.sf.iwant.api.CacheScopeChoices;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.Source;
import net.sf.iwant.api.Target;
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
			return network.cacheLocation(src);
		}

	}

	@Override
	public File contentDescriptorOf(Target target) {
		return new File(wsCache, "descriptor/" + target.name());
	}

}