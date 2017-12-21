package org.fluentjava.iwant.core;

import java.io.File;
import java.io.IOException;

class Refresher {

	private final TimestampReader timestampReader;
	private final ContentDescriptionCache contentDescriptionCache;
	private final File temporaryDirectory;
	private final Locations locations;

	Refresher(TimestampReader timestampReader,
			ContentDescriptionCache contentDescriptionCache,
			File temporaryDirectory, Locations locations) {
		this.timestampReader = timestampReader;
		this.contentDescriptionCache = contentDescriptionCache;
		this.temporaryDirectory = temporaryDirectory;
		this.locations = locations;
	}

	static Refresher forReal(Locations locations) {
		return new Refresher(new TimestampReaderFileImpl(locations),
				new ContentDescriptionCacheFileImpl(
						locations.contentDescriptionCacheDir()), new File(
						locations.temporaryDirectory()), locations);
	}

	void refresh(Target<?> target) throws Exception {
		for (Target<?> dependency : target.dependencies()) {
			refresh(dependency);
		}
		if (needsRefreshing(target)) {
			doRefresh(target);
		}
	}

	private boolean needsRefreshing(Target<?> target) throws IOException {
		Long targetTimestamp = timestampReader.modificationTime(target);
		if (targetTimestamp == null) {
			TextOutput.debugLog(target
					+ " needs refreshing, because it is missing.");
			return true;
		}
		if (hasContentDefinitionChanged(target)) {
			TextOutput.debugLog(target
					+ " needs refreshing, because content definition changed.");
			return true;
		}
		for (Path ingredient : target.ingredients()) {
			Long sourceTimestamp = timestampReader.modificationTime(ingredient);
			if (sourceTimestamp == null) {
				// Deleted source is modified source; let the content decide
				// whether this is OK or not.
				// TODO should we log a warning, in case the user would like to
				// remove the source declaration
				TextOutput.debugLog(target
						+ " needs refreshing, because ingredient missing: "
						+ ingredient);
				return true;
			}
			if (sourceTimestamp > targetTimestamp) {
				TextOutput.debugLog(target
						+ " needs refreshing, because its timestamp "
						+ targetTimestamp + " <= " + sourceTimestamp
						+ " of its ingredient " + ingredient + " ("
						+ ingredient.asAbsolutePath(locations) + ")");
				return true;
			}
		}
		// no ingredient nor content definition modified
		return false;
	}

	private boolean hasContentDefinitionChanged(Target<?> target)
			throws IOException {
		String cached = contentDescriptionCache
				.retrieveContentDescription(target);
		String current = target.content().definitionDescription();
		return !current.equals(cached);
	}

	private void doRefresh(Target<?> target) throws Exception {
		TextOutput.debugLog("Refreshing " + target);
		File destination = new File(target.asAbsolutePath(locations));
		remove(destination);
		TextOutput.debugLog("Calling refresh of " + target.content());
		target.content().refresh(
				new RefreshEnvironment(destination, temporaryDirectory,
						locations));
		contentDescriptionCache.cacheContentDescription(target);
	}

	private static void remove(File destination) {
		TextOutput.debugLog("Removing outdated " + destination);
		FileUtils.del(destination);
	}

}
