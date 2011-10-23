package net.sf.iwant.core;

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
			TextOutput.debugLog("Needs refreshing, because target missing: "
					+ target);
			return true;
		}
		if (hasContentDefinitionChanged(target)) {
			TextOutput
					.debugLog("Needs refreshing, because content definition changed: "
							+ target);
			return true;
		}
		for (Path ingredient : target.ingredients()) {
			Long sourceTimestamp = timestampReader.modificationTime(ingredient);
			if (sourceTimestamp == null) {
				// Deleted source is modified source; let the content decide
				// whether this is OK or not.
				// TODO should we log a warning, in case the user would like to
				// remove the source declaration
				TextOutput
						.debugLog("Needs refreshing, because ingredient missing: "
								+ target + " (ingredient: " + ingredient + ")");
				return true;
			}
			if (sourceTimestamp > targetTimestamp) {
				TextOutput
						.debugLog("Needs refreshing, because ingredient changed: "
								+ target + " (ingredient: " + ingredient + ")");
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
		target.content().refresh(
				new RefreshEnvironment(new File(target
						.asAbsolutePath(locations)), temporaryDirectory,
						locations));
		contentDescriptionCache.cacheContentDescription(target);
	}

}
