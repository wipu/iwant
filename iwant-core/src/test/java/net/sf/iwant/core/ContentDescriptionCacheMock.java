package net.sf.iwant.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentDescriptionCacheMock implements ContentDescriptionCache {

	private final Map<Target<?>, String> cache = new HashMap<Target<?>, String>();
	private final List<Target<?>> recachedTargets = new ArrayList<Target<?>>();

	public void alreadyContains(Target target, String description) {
		cache.put(target, description);
	}

	public List<Target<?>> recachedTargets() {
		return recachedTargets;
	}

	public void cacheContentDescription(Target target) {
		cache.put(target, target.content().definitionDescription());
		recachedTargets.add(target);
	}

	public String retrieveContentDescription(Target target) {
		return cache.get(target);
	}

}
