package net.sf.iwant.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import net.sf.iwant.io.StreamUtil;

public class HelloTarget implements Target {

	private final String name;
	private final String message;

	public HelloTarget(String name, String message) {
		this.name = name;
		this.message = message;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public InputStream content() {
		return new ByteArrayInputStream(message.getBytes());
	}

	@Override
	public void refreshTo(File cachedContent) throws Exception {
		FileOutputStream out = new FileOutputStream(cachedContent);
		StreamUtil.pipe(content(), out);
		out.close();
	}

}