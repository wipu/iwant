package org.fluentjava.iwant.entry.tests;

import java.net.MalformedURLException;
import java.net.URL;

import org.fluentjava.iwant.entry.Iwant.UnmodifiableSource;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableUrl;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableZip;
import org.junit.jupiter.api.Test;

import com.google.common.testing.EqualsTester;

public class UnmodifiableSourceTest {

	@Test
	public void equalsComparesTypeAndLocation() throws MalformedURLException {
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
