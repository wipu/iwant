package org.fluentjava.iwant.core;

import java.util.HashMap;
import java.util.Map;

class TimestampReaderMock implements TimestampReader {

	private final Map<String, Status> content = new HashMap<String, Status>();
	private final Locations locations;

	TimestampReaderMock(Locations locations) {
		this.locations = locations;
	}

	private abstract class Status {

		public abstract Long modificationTime();

	}

	private class Missing extends Status {

		@Override
		public Long modificationTime() {
			return null;
		}

	}

	private class Exists extends Status {

		private final long modificationTime;

		public Exists(long modificationTime) {
			this.modificationTime = modificationTime;
		}

		@Override
		public Long modificationTime() {
			return modificationTime;
		}

	}

	public void doesNotExist(String path) {
		content.put(path, new Missing());
	}

	public void modifiedAt(String path, long modificationTime) {
		content.put(path, new Exists(modificationTime));
	}

	@Override
	public Long modificationTime(Path path) {
		Status status = content.get(path.asAbsolutePath(locations));
		if (status == null) {
			throw new IllegalStateException("Please tell me about " + path);
		}
		return status.modificationTime();
	}

}
