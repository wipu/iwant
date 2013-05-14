package net.sf.iwant.wsdef;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.api.ScriptGenerated;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;

public class Descripted extends Target {

	private final Source doc;
	private final Source descript;
	private final Source maybeIwantWsroot;
	private final Source abstractArticle;
	private final Path maybeInitialState;
	private final Path tutorialWsdefSrc;

	public Descripted(String namePrefix, String docName, Path tutorialWsdefSrc,
			Source maybeIwantWsroot, Path maybeInitialState) {
		super(namePrefix + docName + ".html");
		this.tutorialWsdefSrc = tutorialWsdefSrc;
		this.maybeIwantWsroot = maybeIwantWsroot;
		this.maybeInitialState = maybeInitialState;
		this.doc = Source.underWsroot("iwant-docs/src/main/descript/tutorial/"
				+ docName + ".sh");
		this.abstractArticle = Source
				.underWsroot("iwant-docs/src/main/descript/tutorial/abstract-article.sh");
		this.descript = Source.underWsroot("iwant-lib-descript/descript.sh");
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public boolean supportsParallelism() {
		// TODO return true when known to work
		return false;
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.add(doc);
		ingredients.add(abstractArticle);
		ingredients.add(descript);
		ingredients.add(tutorialWsdefSrc);
		if (maybeIwantWsroot != null) {
			ingredients.add(maybeIwantWsroot);
		}
		if (maybeInitialState != null) {
			ingredients.add(maybeInitialState);
		}
		ingredients.add(Source
				.underWsroot("as-iwant-developer/i-have/wsdef/src/main/java/"
						+ "net/sf/iwant/wsdef/Descripted.java"));
		return ingredients;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		File html = new File(dest, "doc.html");

		File iwantWsRoot = maybeIwantWsroot == null ? null : ctx
				.cached(maybeIwantWsroot);
		File initialState = maybeInitialState == null ? null : new File(
				ctx.cached(maybeInitialState), "final-state.tar");

		StringBuilder sh = new StringBuilder();
		sh.append("#!/bin/bash\n");
		sh.append("set -eu\n");
		sh.append("export IWANT_TUTORIAL_WSDEF_SRC="
				+ ctx.cached(tutorialWsdefSrc) + "\n");
		if (iwantWsRoot != null) {
			sh.append("export LOCAL_IWANT_WSROOT=" + iwantWsRoot + "\n");
		}
		if (initialState != null) {
			sh.append("export INITIAL_STATE=" + initialState + "\n");
		}
		sh.append("DOC=combined.sh\n");
		sh.append("cat " + ctx.cached(abstractArticle) + " " + ctx.cached(doc)
				+ " > \"$DOC\"\n");
		sh.append("'" + ctx.cached(descript) + "' \"$DOC\" '" + html
				+ "' true\n");

		File script = Iwant.newTextFile(new File(dest, name() + ".sh"),
				sh.toString());
		script.setExecutable(true);
		ScriptGenerated.execute(dest,
				new String[] { script.getCanonicalPath() });
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

}
