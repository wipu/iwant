package net.sf.iwant.core;

import java.io.File;
import java.io.IOException;

public interface Content {

	void refresh(File destination) throws IOException;

}
