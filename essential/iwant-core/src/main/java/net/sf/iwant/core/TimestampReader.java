package net.sf.iwant.core;

interface TimestampReader {

	/**
	 * @return modification time if path exists, null otherwise
	 */
	Long modificationTime(Path path);

}
