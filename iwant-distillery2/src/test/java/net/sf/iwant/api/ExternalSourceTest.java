package net.sf.iwant.api;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class ExternalSourceTest extends TestCase {

	public void testCachedAtIsFileAsSuch() throws IOException {
		File file = new File("/any/file");
		ExternalSource es = new ExternalSource(file);
		TargetEvaluationContext ctx = null; // any
		assertSame(file, es.cachedAt(ctx));
	}

}
