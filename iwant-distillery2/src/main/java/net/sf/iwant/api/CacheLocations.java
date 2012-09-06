package net.sf.iwant.api;

import java.io.File;

public interface CacheLocations {

	File modifiableTargets();

	File wsRoot();

	File cachedDescriptors();

}
