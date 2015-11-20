package net.sf.iwant.entry.tests;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.testing.EqualsTester;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant.UnmodifiableSource;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.entry.Iwant.UnmodifiableZip;

public class UnmodifiableSourceTest extends TestCase {

	public void testEqualsComparesTypeAndLocation()
			throws MalformedURLException {
		UnmodifiableSource<URL> url1a = new UnmodifiableUrl(
				new URL("http://localhost/one"));
		UnmodifiableSource<URL> url1b = new UnmodifiableUrl(url1a.location());
		UnmodifiableSource<URL> url2 = new UnmodifiableUrl(
				new URL("http://localhost/two"));
		UnmodifiableSource<URL> zipFromUrl1 = new UnmodifiableZip(
				url1a.location());

		EqualsTester et = new EqualsTester();
		et.addEqualityGroup(url1a, url1b);
		et.addEqualityGroup(url2);
		et.addEqualityGroup(zipFromUrl1);
		et.testEquals();
	}

}
