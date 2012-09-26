package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public class WsInfoMock extends WsInfo {

	public WsInfoMock() throws IOException {
		super(new StringReader(src()), new File("mocked/wsinfo"));
	}

	private static String src() {
		StringBuilder b = new StringBuilder();
		b.append("# paths are relative to this file's directory\n");
		b.append("WSNAME=example\n");
		b.append("WSROOT=../..\n");
		b.append("WSDEF_SRC=wsdef\n");
		b.append("WSDEF_CLASS=com.example.wsdef.Workspace\n");
		return b.toString();
	}

	@Override
	public String wsdefClass() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public String wsdefdefClassSimpleName() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public File wsdefdefJava() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public String wsdefdefPackage() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public File wsdefdefSrc() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public String wsName() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public File wsRoot() {
		throw new UnsupportedOperationException("TODO test and implement");
	}
}
