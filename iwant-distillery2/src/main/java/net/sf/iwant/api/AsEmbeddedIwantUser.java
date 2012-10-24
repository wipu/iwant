package net.sf.iwant.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry3.Caches;
import net.sf.iwant.entry3.CachesImpl;
import net.sf.iwant.entry3.WishEvaluator;

/**
 * TODO this belongs out of API because it depends on implementations, a
 * dedicated module later for this.
 * 
 * (what API are we even talking about, API for embedded user or target author?)
 */
public class AsEmbeddedIwantUser {

	public static IHavePlease with() {
		return new IHavePlease();
	}

	public static class IHavePlease {

		private File wsRoot;
		private File wsCache;

		public IHavePlease workspaceAt(File wsRoot) {
			this.wsRoot = wsRoot;
			return this;
		}

		public IHavePlease cacheAt(File wsCache) {
			this.wsCache = wsCache;
			return this;
		}

		public TargetOrSideEffect iwant() {
			return new TargetOrSideEffect();
		}

		public class TargetOrSideEffect {

			public PathOrContent target(Target target) {
				return new PathOrContent(target);
			}

			public class PathOrContent {

				private final Target target;

				public PathOrContent(Target target) {
					this.target = target;
				}

				public File asPath() {
					try {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						OutputStream err = System.err;
						File iwantApiClasses = null;
						Iwant iwant = Iwant.usingRealNetwork();
						WsInfo wsInfo = null;
						JavaClasses wsdDefClassesTarget = null;
						Caches caches = new CachesImpl(wsCache,
								iwantApiClasses, iwant.network());
						int workerCount = 1;
						WishEvaluator evaluator = new WishEvaluator(out, err,
								wsRoot, iwant, wsInfo, wsdDefClassesTarget,
								caches, workerCount);
						evaluator.asPath(target);
						out.close();
						String cachedTarget = out.toString();
						// TODO refactoring needed, we shouldn't be parsing
						// strings from our own code
						cachedTarget = cachedTarget.replaceAll("\n", "");
						return new File(cachedTarget);
					} catch (RuntimeException e) {
						throw e;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

			}

		}

	}

}
