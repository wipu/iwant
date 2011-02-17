package net.sf.iwant.core;

import java.io.File;
import java.io.IOException;

class Refresher {

	private final TimestampReader timestampReader;
	private final ContentDescriptionCache contentDescriptionCache;
	private final File temporaryDirectory;

	Refresher(TimestampReader timestampReader,
			ContentDescriptionCache contentDescriptionCache,
			File temporaryDirectory) {
		this.timestampReader = timestampReader;
		this.contentDescriptionCache = contentDescriptionCache;
		this.temporaryDirectory = temporaryDirectory;
	}

	static Refresher forReal(Locations locations) {
		return new Refresher(new TimestampReaderFileImpl(),
				new ContentDescriptionCacheFileImpl(
						locations.contentDescriptionCacheDir()), new File(
						locations.temporaryDirectory()));
	}

	void refresh(Target target) throws Exception {
		for (Target dependency : target.dependencies()) {
			refresh(dependency);
		}
		if (needsRefreshing(target))
			doRefresh(target);
	}

	private boolean needsRefreshing(Target target) throws IOException {
		Long targetTimestamp = timestampReader.modificationTime(target);
		if (targetTimestamp == null)
			return true;
		if (hasContentDefinitionChanged(target))
			return true;
		for (Path ingredient : target.ingredients()) {
			Long sourceTimestamp = timestampReader.modificationTime(ingredient);
			if (sourceTimestamp == null) {
				// Deleted source is modified source; let the content decide
				// whether this is OK or not.
				// TODO should we log a warning, in case the user would like to
				// remove the source declaration
				return true;
			}
			if (sourceTimestamp > targetTimestamp) {
				return true;
			}
		}
		// no ingredient nor content definition modified
		return false;
	}

	private boolean hasContentDefinitionChanged(Target target)
			throws IOException {
		String cached = contentDescriptionCache
				.retrieveContentDescription(target);
		String current = target.content().definitionDescription();
		return !current.equals(cached);
	}

	private void doRefresh(Target target) throws Exception {
		target.content().refresh(
				new RefreshEnvironment(new File(target.name()),
						temporaryDirectory));
		contentDescriptionCache.cacheContentDescription(target);
	}

}
