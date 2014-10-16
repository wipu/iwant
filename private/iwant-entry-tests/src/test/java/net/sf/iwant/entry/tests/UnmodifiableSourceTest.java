package net.sf.iwant.entry.tests;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant.UnmodifiableSource;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.entry.Iwant.UnmodifiableZip;

public class UnmodifiableSourceTest extends TestCase {

	public void testEqualsComparesTypeAndLocation()
			throws MalformedURLException {
		UnmodifiableSource<URL> url1a = new UnmodifiableUrl(new URL(
				"http://localhost/one"));
		UnmodifiableSource<URL> url1b = new UnmodifiableUrl(url1a.location());
		UnmodifiableSource<URL> url2 = new UnmodifiableUrl(new URL(
				"http://localhost/two"));
		UnmodifiableSource<URL> zipFromUrl1 = new UnmodifiableZip(
				url1a.location());

		assertEquals(url1a, url1b);
		assertFalse(url1a.equals(url2));
		assertFalse(url1a.equals(zipFromUrl1));
		assertFalse(url1a.equals("not UnmodifiableSource"));
	}

}
