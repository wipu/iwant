package org.fluentjava.iwant.core;

import java.io.File;

import junit.framework.TestCase;

public class RefresherTest extends TestCase {

	private static final Locations LOCATIONS = new Locations("wsRoot",
			"as-someone", "cacheDir", "iwant-libs");
	private TimestampReaderMock ts;
	private ContentDescriptionCacheMock descrCache;
	private Refresher refresher;
	private ContentMock content;

	@Override
	public void setUp() {
		ts = new TimestampReaderMock(LOCATIONS);
		descrCache = new ContentDescriptionCacheMock();
		refresher = new Refresher(ts, descrCache, new File("tmp"), LOCATIONS);
		content = new ContentMock();
	}

	public void testMissingTargetWithNoSrcIsRefreshed() throws Exception {
		ts.doesNotExist("cacheDir/target/missing");
		Target<ContentMock> target = new Target<ContentMock>("missing", content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/missing]", content
				.refreshedDestinations().toString());
	}

	public void testMissingTargetWithSrcIsRefreshed() throws Exception {
		content.ingredients().add(new Source("src"));
		ts.modifiedAt("wsRoot/src", 1);
		ts.doesNotExist("cacheDir/target/classes");
		Target<ContentMock> target = new Target<ContentMock>("classes", content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
	}

	public void testExistingTargetWithNoSrcAndUnchangedDescrIsNotRefreshed()
			throws Exception {
		ts.modifiedAt("cacheDir/target/constant", 1);
		Target<ContentMock> target = new Target<ContentMock>("constant",
				content);
		descrCache.alreadyContains(target, content.definitionDescription());
		refresher.refresh(target);
		assertEquals("[]", content.refreshedDestinations().toString());
	}

	public void testExistingTargetWithNoSrcAndNoCachedDescrIsRefreshed()
			throws Exception {
		ts.modifiedAt("cacheDir/target/constant", 1);
		Target<ContentMock> target = new Target<ContentMock>("constant",
				content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/constant]", content
				.refreshedDestinations().toString());
	}

	public void testExistingTargetWithMissingSrcIsRefreshed() throws Exception {
		content.ingredients().add(new Source("src"));
		ts.modifiedAt("cacheDir/target/classes", 1);
		ts.doesNotExist("wsRoot/src");
		Target<ContentMock> target = new Target<ContentMock>("classes", content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
	}

	public void testTargetOlderThanItsSourceIsRefreshed() throws Exception {
		content.ingredients().add(new Source("src"));
		ts.modifiedAt("cacheDir/target/classes", 1);
		ts.modifiedAt("wsRoot/src", 2);
		Target<ContentMock> target = new Target<ContentMock>("classes", content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
	}

	public void testTargetNotOlderThanItsSourceAndUnchangedContentDescriptionIsNotRefreshed()
			throws Exception {
		content.ingredients().add(new Source("src"));
		ts.modifiedAt("wsRoot/src", 1);
		ts.modifiedAt("cacheDir/target/classes", 1);
		Target<ContentMock> target = new Target<ContentMock>("classes", content);
		descrCache.alreadyContains(target, content.definitionDescription());
		refresher.refresh(target);
		assertEquals("[]", content.refreshedDestinations().toString());
		assertEquals("[]", descrCache.recachedTargets().toString());
	}

	public void testTargetNotOlderThanItsSourceIsRefreshedIfItsContentDescriptionHasChanged()
			throws Exception {
		content.ingredients().add(new Source("src"));
		content.definitionDescription("new-description");
		ts.modifiedAt("wsRoot/src", 1);
		ts.modifiedAt("cacheDir/target/classes", 1);
		Target<ContentMock> target = new Target<ContentMock>("classes", content);
		descrCache.alreadyContains(target, "old-description");
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
		assertEquals("[Target:classes]", descrCache.recachedTargets()
				.toString());
		assertEquals("new-description",
				descrCache.retrieveContentDescription(target));
	}

}
