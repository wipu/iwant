package net.sf.iwant.apimocks;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.coreservices.StreamUtil;
import net.sf.iwant.entrymocks.NullCheck;

public class TargetMock extends Target {

	private List<Path> ingredients;
	private String content;
	private String contentDescriptor;
	private boolean supportsParallelism = true;
	private String errorMessageToThrowAfterCreatingCachedContent;
	private int timesPathWasCalled;

	public TargetMock(String name) {
		super(name);
	}

	public static TargetMock ingredientless(String name) {
		TargetMock jar = new TargetMock(name);
		jar.hasNoIngredients();
		return jar;
	}

	@Override
	public synchronized InputStream content(TargetEvaluationContext ctx)
			throws Exception {
		return new ByteArrayInputStream(NullCheck.nonNull(content).getBytes());
	}

	public synchronized void hasContent(String content) {
		this.content = content;
	}

	@Override
	public synchronized void path(TargetEvaluationContext ctx)
			throws Exception {
		timesPathWasCalled++;
		StreamUtil.pipeAndClose(content(ctx),
				new FileOutputStream(ctx.cached(this)));
		if (errorMessageToThrowAfterCreatingCachedContent != null) {
			throw new IllegalStateException(
					errorMessageToThrowAfterCreatingCachedContent);
		}
	}

	@Override
	public synchronized List<Path> ingredients() {
		return NullCheck.nonNull(ingredients);
	}

	public synchronized void hasIngredients(List<Path> ingredients) {
		this.ingredients = ingredients;
	}

	public synchronized void hasIngredients(Path... ingredients) {
		hasIngredients(Arrays.asList(ingredients));
	}

	public void hasNoIngredients() {
		hasIngredients(Collections.<Path> emptyList());
	}

	@Override
	public synchronized String contentDescriptor() {
		return NullCheck.nonNull(contentDescriptor);
	}

	public synchronized void hasContentDescriptor(String contentDescriptor) {
		this.contentDescriptor = contentDescriptor;
	}

	@Override
	public synchronized boolean supportsParallelism() {
		return supportsParallelism;
	}

	public synchronized void doesNotSupportParallelism() {
		supportsParallelism = false;
	}

	public synchronized void shallFailAfterCreatingCachedContent(
			String errorMessage) {
		this.errorMessageToThrowAfterCreatingCachedContent = errorMessage;
	}

	public synchronized void shallNotFailAfterCreatingCachedContent() {
		shallFailAfterCreatingCachedContent(null);
	}

	public synchronized int timesPathWasCalled() {
		return timesPathWasCalled;
	}

}
