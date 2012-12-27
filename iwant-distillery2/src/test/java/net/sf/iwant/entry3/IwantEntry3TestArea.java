package net.sf.iwant.entry3;

import java.io.File;

import junit.framework.Assert;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testarea.TestArea;

public class IwantEntry3TestArea extends TestArea {

	public File hasFile(String path, String content) {
		File file = new File(root(), path);
		file.getParentFile().mkdirs();
		Iwant.writeTextFile(file, content);
		return file;
	}

	public void shallContainFragmentIn(String path, String fragment) {
		String actual = contentOf(path);
		if (!actual.contains(fragment)) {
			Assert.assertEquals("File " + path + "\nshould contain:\n"
					+ fragment, actual);
		}
	}

}
