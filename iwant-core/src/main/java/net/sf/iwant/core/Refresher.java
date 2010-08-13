package net.sf.iwant.core;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;

class Refresher {

	private final TimestampReader timestampReader;
	private final ContentDescriptionCache contentDescriptionCache;

	Refresher(TimestampReader timestampReader,
			ContentDescriptionCache contentDescriptionCache) {
		this.timestampReader = timestampReader;
		this.contentDescriptionCache = contentDescriptionCache;
	}

	static Refresher forReal(Locations locations) {
		return new Refresher(new TimestampReaderFileImpl(),
				new ContentDescriptionCacheFileImpl(
						locations.contentDescriptionCacheDir()));
	}

	void refresh(Target target) throws Exception {
		Content content = target.content();
		for (Target dependency : content.dependencies()) {
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
		SortedSet<Path> sources = target.content().sources();
		for (Path source : sources) {
			Long sourceTimestamp = timestampReader.modificationTime(source);
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
		// no source nor content definition modified
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
		target.content().refresh(new File(target.name()));
		contentDescriptionCache.cacheContentDescription(target);
	}

}
