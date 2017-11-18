package org.fluentjava.iwant.core;

import java.io.IOException;

interface ContentDescriptionCache {

	String retrieveContentDescription(Target<?> target) throws IOException;

	void cacheContentDescription(Target<?> target) throws IOException;

}
