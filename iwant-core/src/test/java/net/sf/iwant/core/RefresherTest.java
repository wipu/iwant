package net.sf.iwant.core;

import junit.framework.TestCase;

public class RefresherTest extends TestCase {

	private static final Locations LOCATIONS = new Locations("wsRoot",
			"cacheDir");
	private TimestampReaderMock ts;
	private Refresher refresher;
	private ContentMock content;

	public void setUp() {
		ts = new TimestampReaderMock();
		refresher = new Refresher(ts);
		content = new ContentMock();
	}

	public void testMissingTargetWithNoSrcIsRefreshed() throws Exception {
		ts.doesNotExist("cacheDir/missing");
		Target target = new Target("missing", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[cacheDir/missing]", content.refreshedDestinations()
				.toString());
	}

	public void testMissingTargetWithSrcIsRefreshed() throws Exception {
		content.sources().add(new Source("src", LOCATIONS));
		ts.modifiedAt("wsRoot/src", 1);
		ts.doesNotExist("cacheDir/classes");
		Target target = new Target("classes", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[cacheDir/classes]", content.refreshedDestinations()
				.toString());
	}

	public void testExistingTargetWithNoSrcIsNotRefreshed() throws Exception {
		ts.modifiedAt("cacheDir/constant", 1);
		Target target = new Target("constant", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[]", content.refreshedDestinations().toString());
	}

	public void testExistingTargetWithMissingSrcIsRefreshed() throws Exception {
		content.sources().add(new Source("src", LOCATIONS));
		ts.modifiedAt("cacheDir/classes", 1);
		ts.doesNotExist("wsRoot/src");
		Target target = new Target("classes", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[cacheDir/classes]", content.refreshedDestinations()
				.toString());
	}

	public void testTargetOlderThanItsSourceIsRefreshed() throws Exception {
		content.sources().add(new Source("src", LOCATIONS));
		ts.modifiedAt("cacheDir/classes", 1);
		ts.modifiedAt("wsRoot/src", 2);
		Target target = new Target("classes", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[cacheDir/classes]", content.refreshedDestinations()
				.toString());
	}

	public void testTargetNotOlderThanItsSourceIsNotRefreshed()
			throws Exception {
		content.sources().add(new Source("src", LOCATIONS));
		ts.modifiedAt("wsRoot/src", 1);
		ts.modifiedAt("cacheDir/classes", 1);
		Target target = new Target("classes", LOCATIONS, content);
		refresher.refresh(target);
		assertEquals("[]", content.refreshedDestinations().toString());
	}

}
