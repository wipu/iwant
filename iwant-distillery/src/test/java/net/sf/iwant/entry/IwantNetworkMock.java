package net.sf.iwant.entry;

import java.io.File;
import java.net.URL;

import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testarea.TestArea;

public class IwantNetworkMock implements IwantNetwork {

	private final TestArea testArea;
	private File wantedUnmodifiable;

	public IwantNetworkMock(TestArea testArea) {
		this.testArea = testArea;
	}

	public File wantedUnmodifiable() {
		if (wantedUnmodifiable == null) {
			wantedUnmodifiable = testArea.newDir("wanted-unmodifiable");
		}
		return wantedUnmodifiable;
	}

	public String messages() {
		// TODO Auto-generated method stub
		return "lkj";
	}

	public URL svnkitUrl() {
		return getClass().getResource("dir-containing-a-and-b.zip");
	}

}
