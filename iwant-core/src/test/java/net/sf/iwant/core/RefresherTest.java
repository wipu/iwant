package net.sf.iwant.core;

import java.io.File;

import junit.framework.TestCase;

public class RefresherTest extends TestCase {

	private static final Locations LOCATIONS = new Locations("wsRoot",
			"cacheDir");
	private TimestampReaderMock ts;
	private ContentDescriptionCacheMock descrCache;
	private Refresher refresher;
	private ContentMock content;

	public void setUp() {
		ts = new TimestampReaderMock();
		descrCache = new ContentDescriptionCacheMock();
		refresher = new Refresher(ts, descrCache, new File("tmp"));
		content = new ContentMock();
	}

	public void testMissingTargetWithNoSrcIsRefreshed() throws Exception {
		ts.doesNotExist("cacheDir/target/missing");
		Target target = new Target("missing", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/missing]", content
				.refreshedDestinations().toString());
	}

	public void testMissingTargetWithSrcIsRefreshed() throws Exception {
		content.sources().add(new Source("src", LOCATIONS));
		ts.modifiedAt("wsRoot/src", 1);
		ts.doesNotExist("cacheDir/target/classes");
		Target target = new Target("classes", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
	}

	public void testExistingTargetWithNoSrcAndUnchangedDescrIsNotRefreshed()
			throws Exception {
		ts.modifiedAt("cacheDir/target/constant", 1);
		Target target = new Target("constant", LOCATIONS, content);
		descrCache.alreadyContains(target, content.definitionDescription());
		refresher.refresh(target);
		assertEquals("[]", content.refreshedDestinations().toString());
	}

	public void testExistingTargetWithNoSrcAndNoCachedDescrIsRefreshed()
			throws Exception {
		ts.modifiedAt("cacheDir/target/constant", 1);
		Target target = new Target("constant", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/constant]", content
				.refreshedDestinations().toString());
	}

	public void testExistingTargetWithMissingSrcIsRefreshed() throws Exception {
		content.sources().add(new Source("src", LOCATIONS));
		ts.modifiedAt("cacheDir/target/classes", 1);
		ts.doesNotExist("wsRoot/src");
		Target target = new Target("classes", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
	}

	public void testTargetOlderThanItsSourceIsRefreshed() throws Exception {
		content.sources().add(new Source("src", LOCATIONS));
		ts.modifiedAt("cacheDir/target/classes", 1);
		ts.modifiedAt("wsRoot/src", 2);
		Target target = new Target("classes", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
	}

	public void testTargetNotOlderThanItsSourceAndUnchangedContentDescriptionIsNotRefreshed()
			throws Exception {
		content.sources().add(new Source("src", LOCATIONS));
		ts.modifiedAt("wsRoot/src", 1);
		ts.modifiedAt("cacheDir/target/classes", 1);
		Target target = new Target("classes", LOCATIONS, content);
		descrCache.alreadyContains(target, content.definitionDescription());
		refresher.refresh(target);
		assertEquals("[]", content.refreshedDestinations().toString());
		assertEquals("[]", descrCache.recachedTargets().toString());
	}

	public void testTargetNotOlderThanItsSourceIsRefreshedIfItsContentDescriptionHasChanged()
			throws Exception {
		content.sources().add(new Source("src", LOCATIONS));
		content.definitionDescription("new-description");
		ts.modifiedAt("wsRoot/src", 1);
		ts.modifiedAt("cacheDir/target/classes", 1);
		Target target = new Target("classes", LOCATIONS, content);
		descrCache.alreadyContains(target, "old-description");
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
		assertEquals("[Target:cacheDir/target/classes]", descrCache
				.recachedTargets().toString());
		assertEquals("new-description",
				descrCache.retrieveContentDescription(target));
	}

}
