package org.fluentjava.iwant.entry3;

import java.io.File;
import java.net.URL;

import org.fluentjava.iwant.api.model.CacheScopeChoices;
import org.fluentjava.iwant.api.model.Caches;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantNetwork;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableUrl;

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

		// earlier tmpDir was used as such, now we are saving
		// SSD and using /tmp instead, simply putting tmpDir as
		// child dir for isolation:
		tmpDir = new File(Iwant.IWANT_GLOBAL_TMP_DIR + "/" + tmpDir);

		Iwant.del(tmpDir);
		Iwant.mkdirs(tmpDir);
		return tmpDir;
	}

}
