package net.sf.iwant.apimocks;

import java.io.File;

import net.sf.iwant.api.model.WsInfo;
import net.sf.iwant.entrymocks.NullCheck;

public class WsInfoMock implements WsInfo {

	private File wsdefdefModule;
	private String wsName;
	private File wsRoot;
	private String relativeAsSomeone;

	@Override
	public String wsName() {
		return NullCheck.nonNull(wsName);
	}

	public void hasWsName(String wsName) {
		this.wsName = wsName;
	}

	@Override
	public File wsRoot() {
		return NullCheck.nonNull(wsRoot);
	}

	public void hasWsRoot(File wsRoot) {
		this.wsRoot = wsRoot;
	}

	@Override
	public File wsdefdefModule() {
		return NullCheck.nonNull(wsdefdefModule);
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
		return NullCheck.nonNull(relativeAsSomeone);
	}

}
