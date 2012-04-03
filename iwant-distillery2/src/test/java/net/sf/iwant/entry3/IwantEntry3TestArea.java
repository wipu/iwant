package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testarea.TestArea;

public class IwantEntry3TestArea extends TestArea {

	public void hasFile(String path, String content) {
		try {
			File file = new File(root(), path);
			Iwant.ensureDir(file.getParentFile());
			new FileWriter(file).append(content).close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
