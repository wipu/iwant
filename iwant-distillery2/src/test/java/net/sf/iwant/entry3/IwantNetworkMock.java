package net.sf.iwant.entry3;

import java.io.File;
import java.net.URL;

import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testarea.TestArea;

public class IwantNetworkMock implements IwantNetwork {

	private final TestArea testArea;

	public IwantNetworkMock(TestArea testArea) {
		this.testArea = testArea;
	}

	public File wantedUnmodifiable(URL url) {
		return new File(testArea.root(), "wanted-unmodifiable");
	}

	public URL svnkitUrl() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	public URL junitUrl() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

}
