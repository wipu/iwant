package net.sf.iwant.apimocks;

import java.io.File;

import net.sf.iwant.api.model.WsInfo;

public class WsInfoMock implements WsInfo {

	private File wsdefdefModule;
	private String wsName;
	private File wsRoot;
	private String relativeAsSomeone;

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
		return nonNull(wsRoot, "wsRoot");
	}

	public void hasWsRoot(File wsRoot) {
		this.wsRoot = wsRoot;
	}

	@Override
	public File wsdefdefModule() {
		return nonNull(wsdefdefModule, "wsdefdefModule");
	}

	public void hasWsdefdefModule(File wsdefdefModule) {
		this.wsdefdefModule = wsdefdefModule;
	}

	@Override
	public File wsdefdefSrc() {
		return new File(wsdefdefModule(), "src/main/java");
	}

	@Override
	public String wsdefdefClass() {
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

	public void hasRelativeAsSomeone(String relativeAsSomeone) {
		this.relativeAsSomeone = relativeAsSomeone;
	}

	@Override
	public String relativeAsSomeone() {
		return nonNull(relativeAsSomeone, "relativeAsSomeone");
	}

}
