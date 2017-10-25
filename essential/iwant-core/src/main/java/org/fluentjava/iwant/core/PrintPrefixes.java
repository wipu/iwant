package net.sf.iwant.core;

class PrintPrefixes {

	public static final String SYSTEM_PROPERTY_NAME = "iwant-print-prefix";

	private final String prefix;
	private final String errPrefix;
	private final String outPrefix;

	public static PrintPrefixes fromSystemProperty() {
		return fromPrefix(System.getProperty(SYSTEM_PROPERTY_NAME));
	}

	public static PrintPrefixes fromPrefix(String prefix) {
		return new PrintPrefixes(prefix);
	}

	private PrintPrefixes(String specifiedPrefix) {
		this.prefix = specifiedPrefix != null ? specifiedPrefix : "";
		this.errPrefix = combined(prefix, "err:");
		this.outPrefix = combined(prefix, "out:");
	}

	private static String combined(String prefix, String postfix) {
		return "".equals(prefix) ? "" : prefix + postfix;
	}

	public String prefix() {
		return prefix;
	}

	public String errPrefix() {
		return errPrefix;
	}

	public String outPrefix() {
		return outPrefix;
	}

	public String multiLineErr(String message) {
		// sigh, StringTokenizer and BufferedReader both make it difficult to
		// handle missing trailing linebreak correctly so DIY.
		StringBuilder out = new StringBuilder();
		boolean lineStart = true;
		for (int i = 0; i < message.length(); i++) {
			if (lineStart) {
				out.append(errPrefix());
				lineStart = false;
			}
			out.append(message.charAt(i));
			if ('\n' == message.charAt(i)) {
				lineStart = true;
			}
		}
		return out.toString();
	}

	@Override
	public String toString() {
		return "PrintPrefixes [prefix=" + prefix + ", errPrefix=" + errPrefix
				+ ", outPrefix=" + outPrefix + "]";
	}

}
