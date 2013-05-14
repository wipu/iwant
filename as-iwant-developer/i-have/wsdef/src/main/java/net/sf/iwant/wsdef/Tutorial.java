package net.sf.iwant.wsdef;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

import org.apache.commons.io.FileUtils;

public class Tutorial extends Target {

	private final Target bootstrappingDoc;
	private final Target creatingWsdefDoc;
	private final Target helloWithEclipseDoc;
	private final Descripted extLibsDoc;

	private Tutorial(String namePrefix, Target bootstrappingDoc) {
		super(namePrefix + "tutorial");
		this.bootstrappingDoc = bootstrappingDoc;
		this.creatingWsdefDoc = new Descripted(namePrefix, "creating-wsdef",
				tutorialWsdefSrc(), null, bootstrappingDoc);
		this.helloWithEclipseDoc = new Descripted(namePrefix,
				"helloworld-with-eclipse", tutorialWsdefSrc(), null,
				creatingWsdefDoc);
		this.extLibsDoc = new Descripted(namePrefix, "ext-libs-in-wsdef",
				tutorialWsdefSrc(), null, creatingWsdefDoc);
	}

	public static Tutorial local() {
		return new Tutorial("local-", new Descripted("",
				"bootstrapping-locally", tutorialWsdefSrc(),
				Source.underWsroot(""), null));
	}

	public static Tutorial remote() {
		return new Tutorial("remote-", new Descripted("",
				"bootstrapping-with-svnexternals", tutorialWsdefSrc(), null,
				null));
	}

	private static Source tutorialWsdefSrc() {
		return Source.underWsroot("iwant-tutorial-wsdefs/src");
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.add(bootstrappingDoc);
		ingredients.add(creatingWsdefDoc);
		ingredients.add(helloWithEclipseDoc);
		ingredients.add(extLibsDoc);
		ingredients.add(Source
				.underWsroot("as-iwant-developer/i-have/wsdef/src/main/java/"
						+ "net/sf/iwant/wsdef/Tutorial.java"));
		return ingredients;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		StringBuilder index = new StringBuilder();
		index.append("<html><body>\n");

		String bootstrapping = page(ctx, "bootstrapping.html", bootstrappingDoc);
		index.append("<a href='" + bootstrapping
				+ "'>Bootstrapping iwant for the workspace</a>\n");

		String creatingWsDef = page(ctx, "creating-wsdef.html",
				creatingWsdefDoc);
		index.append("<a href='" + creatingWsDef
				+ "'>Creating a workspace definition</a>\n");

		String helloWithEclipse = page(ctx, "helloworld-with-eclipse.html",
				helloWithEclipseDoc);
		index.append("<a href='" + helloWithEclipse
				+ "'>Hello world with Eclipse</a>\n");

		String extLibs = page(ctx, "ext-libs-in-wsdef.html", extLibsDoc);
		index.append("<a href='" + extLibs
				+ "'>Using external libraries in workspace definition</a>\n");

		index.append("</body></html>\n");

		FileUtils.writeStringToFile(new File(dest, "index.html"),
				index.toString());
	}

	private String page(TargetEvaluationContext ctx, String fileName,
			Path fromPath) throws IOException {
		File dest = ctx.cached(this);
		File to = new File(dest, fileName);
		File from = new File(ctx.cached(fromPath), "doc.html");
		System.err.println(to.getName() + " <- " + from);
		FileUtils.copyFile(from, to);
		return fileName;
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

}
