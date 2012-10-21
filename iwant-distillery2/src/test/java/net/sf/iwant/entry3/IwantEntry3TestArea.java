package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.iwant.testarea.TestArea;

public class IwantEntry3TestArea extends TestArea {

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
