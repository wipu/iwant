package net.sf.iwant.apimocks;

import java.io.File;
import java.util.List;

import net.sf.iwant.api.model.IngredientDefinitionContext;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.coreservices.IwantCoreServicesImpl;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entrymocks.NullCheck;

public class TargetEvaluationContextMock
		implements TargetEvaluationContext, IngredientDefinitionContext {

	private File wsRoot;
	private final CachesMock caches;
	private final IwantCoreServicesMock iwantCoreServices;
	private List<? extends Target> targets;

	public TargetEvaluationContextMock(Iwant iwant, CachesMock caches) {
		// TODO dependency injection:
		this(caches,
				new IwantCoreServicesMock(new IwantCoreServicesImpl(iwant)));
	}

	private TargetEvaluationContextMock(CachesMock caches,
			IwantCoreServicesMock iwantCoreServices) {
		this.caches = caches;
		this.iwantCoreServices = iwantCoreServices;
	}

	public TargetEvaluationContextMock(TargetEvaluationContextMock o) {
		this(o.caches, o.iwantCoreServices);
	}

	@Override
	public File wsRoot() {
		return NullCheck.nonNull(wsRoot);
	}

	public void hasWsRoot(File wsRoot) {
		this.wsRoot = wsRoot;
	}

	@Override
	public IwantCoreServicesMock iwant() {
		return iwantCoreServices;
	}

	@Override
	public File cached(Path path) {
		return caches.contentOf(path);
	}

	@Override
	public File freshTemporaryDirectory() {
		return caches.temporaryDirectory("mock-worker");
	}

	@Override
	public List<? extends Target> targets() {
		return targets;
	}

	public void setTargets(List<? extends Target> targets) {
		this.targets = targets;
	}

	@Override
	public File locationOf(Source source) {
		return cached(source);
	}

}
