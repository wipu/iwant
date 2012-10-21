package net.sf.iwant.api;

import java.io.File;

public class WsInfoMock implements WsInfo {

	private File wsdefdefSrc;
	private String wsName;

	private <T> T nonNull(T value, Object request) {
		if (value == null) {
			throw new IllegalStateException("You forgot to teach " + request
					+ "\nto " + this);
		}
		return value;
	}

	@Override
	public String wsName() {
		return nonNull(wsName, "wsName");
	}

	public void hasWsName(String wsName) {
		this.wsName = wsName;
	}

	@Override
	public File wsRoot() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public File wsdefdefSrc() {
		return nonNull(wsdefdefSrc, "wsdefdefSrc");
	}

	public void hasWsdefdefSrc(File wsdefdefSrc) {
		this.wsdefdefSrc = wsdefdefSrc;
	}

	@Override
	public String wsdefClass() {
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
	public String wsdefdefClassSimpleName() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

}
