package net.sf.iwant.apimocks;

import java.net.URL;

/**
 * java.net.URL equals and hashCode are broken so this is needed for fast and
 * reliable maps and sets
 */
public class UrlString {

	private final String urlString;

	public UrlString(URL url) {
		this.urlString = url.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((urlString == null) ? 0 : urlString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		UrlString other = (UrlString) obj;
		if (urlString == null) {
			if (other.urlString != null) {
				return false;
			}
		} else if (!urlString.equals(other.urlString)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return urlString;
	}

}
