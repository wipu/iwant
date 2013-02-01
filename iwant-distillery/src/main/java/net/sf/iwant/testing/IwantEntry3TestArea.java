package net.sf.iwant.testing;

import java.io.File;

import junit.framework.Assert;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testarea.TestArea;

public class IwantEntry3TestArea extends TestArea {

	public File hasFile(String path, String content) {
		File file = new File(root(), path);
		return Iwant.newTextFile(file, content);
	}

	public void shallContainFragmentIn(String path, String fragment) {
		String actual = contentOf(path);
		if (!actual.contains(fragment)) {
			Assert.assertEquals("File " + path + "\nshould contain:\n"
					+ fragment, actual);
		}
	}

}
