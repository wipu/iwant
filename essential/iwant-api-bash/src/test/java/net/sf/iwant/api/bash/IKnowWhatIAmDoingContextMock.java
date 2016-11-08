package net.sf.iwant.api.bash;

import java.io.File;
import java.io.OutputStream;
import java.util.Set;

import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.api.model.WsInfo;
import net.sf.iwant.api.wsdef.IKnowWhatIAmDoingContext;
import net.sf.iwant.api.wsdef.IwantPluginWishes;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;

public class IKnowWhatIAmDoingContextMock extends TargetEvaluationContextMock
		implements IKnowWhatIAmDoingContext {

	private JavaSrcModule wsdef;

	public IKnowWhatIAmDoingContextMock(TargetEvaluationContextMock o) {
		super(o);
	}

	@Override
	public WsInfo wsInfo() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public OutputStream err() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public TargetEvaluationContext targetEvaluationContext() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public File iwantFreshCached(Path target) {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public JavaSrcModule wsdefdefJavaModule() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	public void hasWsdefModule(JavaSrcModule wsdef) {
		this.wsdef = wsdef;
	}

	@Override
	public JavaSrcModule wsdefJavaModule() {
		return wsdef;
	}

	@Override
	public Set<? extends JavaModule> iwantApiModules() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public IwantPluginWishes iwantPlugin() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

}
