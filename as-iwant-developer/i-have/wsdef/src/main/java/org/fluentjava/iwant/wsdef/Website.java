package org.fluentjava.iwant.wsdef;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;

public class Website extends Target {

	private final Target tutorial;
	private final Source html;
	private final Target logo;
	private final Target favicon;

	public Website(String name, Target tutorial, Target logo, Target favicon) {
		super(name);
		this.tutorial = tutorial;
		this.logo = logo;
		this.favicon = favicon;
		this.html = Source
				.underWsroot("private/iwant-docs/src/main/html/website");
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<>();
		ingredients.add(tutorial);
		ingredients.add(html);
		ingredients.add(Source
				.underWsroot("as-iwant-developer/i-have/wsdef/src/main/java/"
						+ "net/sf/iwant/wsdef/WorkspaceForIwant.java"));
		ingredients.add(logo);
		ingredients.add(favicon);
		return ingredients;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();
		FileUtils.copyDirectory(ctx.cached(tutorial), dest);
		FileUtils.copyDirectory(ctx.cached(html), dest);
		FileUtils.copyFileToDirectory(ctx.cached(logo), dest);
		FileUtils.copyFileToDirectory(ctx.cached(favicon), dest);
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

}
