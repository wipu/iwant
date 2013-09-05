package net.sf.iwant.wsdef;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

public class Website extends Target {

	private final Target tutorial;
	private final Source html;

	public Website(String name, Target tutorial) {
		super(name);
		this.tutorial = tutorial;
		this.html = Source.underWsroot("iwant-docs/src/main/html/website");
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.add(tutorial);
		ingredients.add(html);
		ingredients.add(Source
				.underWsroot("as-iwant-developer/i-have/wsdef/src/main/java/"
						+ "net/sf/iwant/wsdef/WorkspaceForIwant.java"));
		return ingredients;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();
		FileUtils.copyDirectory(ctx.cached(tutorial), dest);
		FileUtils.copyDirectory(ctx.cached(html), dest);
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName()+":"+ingredients();
	}

}
