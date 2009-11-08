package net.sf.iwant.core;

import java.io.File;
import java.util.SortedSet;

class Refresher {

	private final TimestampReader timestampReader;

	Refresher(TimestampReader timestampReader) {
		this.timestampReader = timestampReader;
	}

	static Refresher forReal() {
		return new Refresher(new TimestampReaderFileImpl());
	}

	void refresh(Target target) throws Exception {
		Content content = target.content();
		for (Target dependency : content.dependencies()) {
			refresh(dependency);
		}
		if (needsRefreshing(target))
			doRefresh(target);
	}

	private boolean needsRefreshing(Target target) {
		Long targetTimestamp = timestampReader.modificationTime(target);
		if (targetTimestamp == null)
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
		// no source was modified
		return false;
	}

	private static void doRefresh(Target target) throws Exception {
		target.content().refresh(new File(target.name()));
	}

}
