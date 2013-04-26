package net.sf.iwant.entry3;

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

public class TargetMock extends Target {

	private List<Path> ingredients;
	private String content;
	private boolean shallNotBeToldToWriteFile;
	private String contentDescriptor;
	private boolean supportsParallelism = true;
	private String errorMessageToThrowAfterCreatingCachedContent;
	private int timesPathWasCalled;

	public TargetMock(String name) {
		super(name);
	}

	public static Path ingredientless(String name) {
		TargetMock jar = new TargetMock(name);
		jar.hasNoIngredients();
		return jar;
	}

	private <T> T nonNull(T value, Object request) {
		if (value == null) {
			throw new IllegalStateException("You forgot to teach " + request
					+ "\nto " + this);
		}
		return value;
	}

	@Override
	public synchronized InputStream content(TargetEvaluationContext ctx)
			throws Exception {
		return new ByteArrayInputStream(nonNull(content, "content").getBytes());
	}

	public synchronized void hasContent(String content) {
		this.content = content;
	}

	@Override
	public synchronized void path(TargetEvaluationContext ctx) throws Exception {
		timesPathWasCalled++;
		if (shallNotBeToldToWriteFile) {
			throw new IllegalStateException(
					"Should not have been told to write to file.");
		}
		StreamUtil.pipeAndClose(content(ctx),
				new FileOutputStream(ctx.cached(this)));
		if (errorMessageToThrowAfterCreatingCachedContent != null) {
			throw new IllegalStateException(
					errorMessageToThrowAfterCreatingCachedContent);
		}
	}

	public synchronized void shallNotBeToldToWriteFile() {
		this.shallNotBeToldToWriteFile = true;
	}

	@Override
	public synchronized List<Path> ingredients() {
		return nonNull(ingredients, "ingredients");
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
		return nonNull(contentDescriptor, "contentDescriptor");
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
