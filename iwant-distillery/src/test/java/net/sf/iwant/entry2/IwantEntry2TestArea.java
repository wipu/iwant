package net.sf.iwant.entry2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.iwant.testarea.TestArea;

public class IwantEntry2TestArea extends TestArea {

	/**
	 * TODO remove redundancy: copy-pasted (but enhanced...) from *3*
	 */
	public File hasFile(String path, String content) {
		try {
			File file = new File(root(), path);
			file.getParentFile().mkdirs();
			new FileWriter(file).append(content).close();
			return file;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
