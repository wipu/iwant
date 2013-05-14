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

	public Descripted(String docName, Source maybeIwantWsroot) {
		super(docName + ".html");
		this.maybeIwantWsroot = maybeIwantWsroot;
		this.doc = Source.underWsroot("iwant-docs/src/main/descript/tutorial/"
				+ docName + ".sh");
		this.descript = Source.underWsroot("iwant-lib-descript/descript.sh");
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.add(doc);
		ingredients.add(descript);
		if (maybeIwantWsroot != null) {
			ingredients.add(maybeIwantWsroot);
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

		File cachedDescript = ctx.cached(descript);
		File cachedDoc = ctx.cached(doc);
		File html = new File(dest, name());

		File iwantWsRoot = maybeIwantWsroot == null ? null : ctx
				.cached(maybeIwantWsroot);

		StringBuilder sh = new StringBuilder();
		sh.append("#!/bin/bash\n");
		sh.append("set -eu\n");
		if (iwantWsRoot != null) {
			sh.append("export LOCAL_IWANT_WSROOT=" + iwantWsRoot + "\n");
		}
		sh.append("'" + cachedDescript + "' '" + cachedDoc + "' '" + html
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
