package net.sf.iwant.entry2;

import java.io.File;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testarea.TestArea;

public class IwantEntry2TestArea extends TestArea {

	/**
	 * TODO remove redundancy: copy-pasted (but enhanced...) from *3*
	 */
	public File hasFile(String path, String content) {
		File file = new File(root(), path);
		file.getParentFile().mkdirs();
		Iwant.writeTextFile(file, content);
		return file;
	}

}
