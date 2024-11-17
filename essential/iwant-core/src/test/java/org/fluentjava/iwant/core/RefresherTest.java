package org.fluentjava.iwant.core;

import java.io.File;

import junit.framework.TestCase;

public class RefresherTest{

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

	@Test public void missingTargetWithNoSrcIsRefreshed() throws Exception {
		ts.doesNotExist("cacheDir/target/missing");
		Target<ContentMock> target = new Target<ContentMock>("missing", content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/missing]", content
				.refreshedDestinations().toString());
	}

	@Test public void missingTargetWithSrcIsRefreshed() throws Exception {
		content.ingredients().add(new Source("src"));
		ts.modifiedAt("wsRoot/src", 1);
		ts.doesNotExist("cacheDir/target/classes");
		Target<ContentMock> target = new Target<ContentMock>("classes", content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
	}

	@Test public void existingTargetWithNoSrcAndUnchangedDescrIsNotRefreshed()
			throws Exception {
		ts.modifiedAt("cacheDir/target/constant", 1);
		Target<ContentMock> target = new Target<ContentMock>("constant",
				content);
		descrCache.alreadyContains(target, content.definitionDescription());
		refresher.refresh(target);
		assertEquals("[]", content.refreshedDestinations().toString());
	}

	@Test public void existingTargetWithNoSrcAndNoCachedDescrIsRefreshed()
			throws Exception {
		ts.modifiedAt("cacheDir/target/constant", 1);
		Target<ContentMock> target = new Target<ContentMock>("constant",
				content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/constant]", content
				.refreshedDestinations().toString());
	}

	@Test public void existingTargetWithMissingSrcIsRefreshed() throws Exception {
		content.ingredients().add(new Source("src"));
		ts.modifiedAt("cacheDir/target/classes", 1);
		ts.doesNotExist("wsRoot/src");
		Target<ContentMock> target = new Target<ContentMock>("classes", content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
	}

	@Test public void targetOlderThanItsSourceIsRefreshed() throws Exception {
		content.ingredients().add(new Source("src"));
		ts.modifiedAt("cacheDir/target/classes", 1);
		ts.modifiedAt("wsRoot/src", 2);
		Target<ContentMock> target = new Target<ContentMock>("classes", content);
		refresher.refresh(target);
		assertEquals("[cacheDir/target/classes]", content
				.refreshedDestinations().toString());
	}

	@Test public void targetNotOlderThanItsSourceAndUnchangedContentDescriptionIsNotRefreshed()
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

	@Test public void targetNotOlderThanItsSourceIsRefreshedIfItsContentDescriptionHasChanged()
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
