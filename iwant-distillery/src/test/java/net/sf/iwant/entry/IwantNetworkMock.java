package net.sf.iwant.entry;

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

	public String messages() {
		// TODO Auto-generated method stub
		return "lkj";
	}

	public URL svnkitUrl() {
		return getClass().getResource("dir-containing-a-and-b.zip");
	}

}
