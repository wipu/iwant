package org.fluentjava.iwant.api.bash;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.fluentjava.iwant.api.core.ScriptGenerated;
import org.fluentjava.iwant.api.model.IngredientDefinitionContext;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.api.wsdef.IKnowWhatIAmDoingContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.coreservices.FileUtil;
import org.fluentjava.iwant.entry.Iwant;

public class TargetImplementedInBash extends TargetBase {

	private final Source script;
	private IngredientDefinitionContext ingrDefCtx;
	private final AtomicReference<List<IngredientTypeNameValue>> ingredientsDefinedInScript = new AtomicReference<>();
	private final List<String> arguments;

	public TargetImplementedInBash(String name, Source script,
			List<String> arguments) {
		super(name);
		this.script = script;
		this.arguments = arguments;
	}

	public static List<TargetImplementedInBash> instancesFromDefaultIndexSh(
			TargetDefinitionContext ctx) {
		String wsdef = ctx.wsdefJavaModule().locationUnderWsRoot();
		return instancesFromIndexSh(ctx,
				Source.underWsroot(wsdef + "/src/main/bash/_index.sh"));
	}

	public static List<TargetImplementedInBash> instancesFromIndexSh(
			TargetDefinitionContext ctx, Path indexSh) {
		try {
			return tryInstancesFrom((IKnowWhatIAmDoingContext) ctx, indexSh);
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	private static List<TargetImplementedInBash> tryInstancesFrom(
			IKnowWhatIAmDoingContext ctx, Path indexSh)
			throws IOException, InterruptedException {
		List<TargetImplementedInBash> instances = new ArrayList<>();
		File tmpDir = ctx.freshTemporaryDirectory();
		File tmpFile = new File(tmpDir, "targets");
		File getTargetsSh = internalScriptReadyToExecute("get-targets.sh");

		File indexShFile = ctx.cached(indexSh);
		if (!indexShFile.exists()) {
			throw new Iwant.IwantException(
					"Please define targets in " + indexShFile);
		}

		List<String> cmdLine = new ArrayList<>();
		cmdLine.add(getTargetsSh.getCanonicalPath());
		cmdLine.add(indexShFile.getCanonicalPath());
		cmdLine.add(tmpFile.getCanonicalPath());

		ScriptGenerated.execute(tmpDir, cmdLine);

		List<String[]> listOfScriptAndArgs = new ArrayList<>();
		if (tmpFile.exists()) {
			listOfScriptAndArgs.addAll(parseIndentedStringArrays(Files
					.lines(tmpFile.toPath()).collect(Collectors.toList())));
		}
		for (String[] scriptAndArgs : listOfScriptAndArgs) {
			String name = scriptAndArgs[0];
			String script = name + ".sh";
			List<String> args = Arrays.asList();
			if (scriptAndArgs.length > 1) {
				script = scriptAndArgs[1];
				args = Arrays.asList(scriptAndArgs).subList(2,
						scriptAndArgs.length);
			}
			String srcDirRelpath = wsRootRelativePath(ctx.wsRoot(),
					indexShFile.getParentFile());
			Source scriptSrc = Source.underWsroot(srcDirRelpath + script);
			TargetImplementedInBash target = new TargetImplementedInBash(name,
					scriptSrc, args);
			instances.add(target);
		}

		return instances;
	}

	private static String wsRootRelativePath(File wsRoot, File file) {
		String srcDirRelpath = FileUtil.relativePathOfFileUnderParent(file,
				wsRoot);
		if (!srcDirRelpath.endsWith("/")) {
			srcDirRelpath = srcDirRelpath + "/";
		}
		if (srcDirRelpath.startsWith("/")) {
			srcDirRelpath = srcDirRelpath.substring(1);
		}
		return srcDirRelpath;
	}

	public Source script() {
		return script;
	}

	public List<String> arguments() {
		return arguments;
	}

	public void setIngredientDefinitionContext(
			IngredientDefinitionContext ingrDefCtx) {
		this.ingrDefCtx = ingrDefCtx;
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		iUse.ingredients("script", script);

		for (IngredientTypeNameValue tnv : ingredientsDefinedInScript()) {
			if ("param".equals(tnv.type)) {
				iUse.parameter(tnv.name, tnv.value);
			} else if ("target-dep".equals(tnv.type)) {
				iUse.ingredients(tnv.name, targetNamed(tnv.value));
			} else if ("source-dep".equals(tnv.type)) {
				iUse.ingredients(tnv.name, Source.underWsroot(tnv.value));
			}
		}
		return iUse.nothingElse();
	}

	private List<IngredientTypeNameValue> ingredientsDefinedInScript() {
		List<IngredientTypeNameValue> ingr = ingredientsDefinedInScript.get();
		// The caching here is not just optimization, it is mandatory.
		// Ingredients are needed during refresh, and if we execute the script,
		// we will recreate the temporary directory, ruining refreshf
		if (ingr == null) {
			// AtomicReference is used because we access it from multiple
			// threads but never concurrently so we don't have a race condition
			// here feven though we do null check and creation as separate steps
			ingr = determineIngredientsDefinedInScript();
			ingredientsDefinedInScript.set(ingr);
		}
		return ingr;
	}

	private List<IngredientTypeNameValue> determineIngredientsDefinedInScript() {
		try {
			return tryDetermineIngredientsDefinedInScript();
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	private List<IngredientTypeNameValue> tryDetermineIngredientsDefinedInScript()
			throws IOException, InterruptedException {
		File tmpDir = ingrDefCtx.freshTemporaryDirectory();
		File tmpFile = new File(tmpDir, "ingredients-and-parameters");

		File getIngrSh = internalScriptReadyToExecute(
				"get-ingredients-and-parameters.sh");

		List<String> cmdLine = new ArrayList<>();
		cmdLine.add(getIngrSh.getCanonicalPath());
		cmdLine.add(ingrDefCtx.locationOf(script).getCanonicalPath());
		cmdLine.add(tmpFile.getCanonicalPath());
		cmdLine.addAll(arguments);

		ScriptGenerated.execute(tmpDir, cmdLine);

		List<IngredientTypeNameValue> retval = new ArrayList<>();
		if (tmpFile.exists()) {
			retval.addAll(parseIngredients(Files.lines(tmpFile.toPath())
					.collect(Collectors.toList())));
		}
		return retval;
	}

	private Target targetNamed(String name) {
		return ingrDefCtx.targets().stream().filter(t -> name.equals(t.name()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"No such target: '" + name + "', only got "
								+ ingrDefCtx.targets()));
	}

	static class IngredientTypeNameValue {

		String type;
		String name;
		String value;

		@Override
		public String toString() {
			return type + "|" + name + "|" + value;
		}

	}

	static List<IngredientTypeNameValue> parseIngredients(List<String> lines) {
		List<String[]> arrs = parseIndentedStringArrays(lines);
		return arrs.stream().map(arr -> ingredientTypeNameValueFrom(arr))
				.collect(Collectors.toList());
	}

	private static IngredientTypeNameValue ingredientTypeNameValueFrom(
			String[] in) {
		IngredientTypeNameValue out = new IngredientTypeNameValue();
		out.type = in[0];
		out.name = in[1];
		out.value = in[2];
		return out;
	}

	static List<String[]> parseIndentedStringArrays(List<String> lines) {
		List<String[]> out = new ArrayList<>();
		List<StringBuilder> bufs = new ArrayList<>();
		for (String line : lines) {
			if (line.startsWith("::")) {
				finishParsingOneStringArrayLine(out, bufs);
				bufs.clear();
				continue;
			}
			if (line.startsWith(": ")) {
				StringBuilder buf = new StringBuilder();
				bufs.add(buf);
				bufs.get(bufs.size() - 1).append(unindented(unindented(line)));
			} else {
				bufs.get(bufs.size() - 1).append("\n").append(unindented(line));
			}
		}
		return out;
	}

	private static void finishParsingOneStringArrayLine(List<String[]> out,
			List<StringBuilder> bufs) {
		String[] arr = new String[bufs.size()];
		for (int i = 0; i < bufs.size(); i++) {
			arr[i] = bufs.get(i).toString();
		}
		out.add(arr);
	}

	private static String unindented(String line) {
		return line.replaceAll("^.", "");
	}

	private static File internalScript(String name) {
		try {
			return new File(
					TargetImplementedInBash.class.getResource(name).toURI());
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	private static File internalScriptReadyToExecute(String name) {
		File f = internalScript(name);
		// for example eclipse copies resources without preserving flags:
		f.setExecutable(true);
		return f;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File refreshPathSh = internalScriptReadyToExecute("refresh-path.sh");

		File tmpDir = ctx.freshTemporaryDirectory();
		File deprefs = new File(tmpDir, "deprefs");
		StringBuilder deprefsContent = new StringBuilder();

		for (Path ingr : ingredients()) {
			deprefsContent.append(ingr.name());
			deprefsContent.append("::");
			deprefsContent.append(ctx.cached(ingr).getCanonicalPath());
			deprefsContent.append("\n");
		}
		FileUtil.newTextFile(deprefs, deprefsContent.toString());

		File runDir = new File(tmpDir, "rundir");
		Iwant.mkdirs(runDir);

		List<String> cmdLine = new ArrayList<>();
		cmdLine.add(refreshPathSh.getCanonicalPath());
		cmdLine.add(ctx.cached(script).getCanonicalPath());
		cmdLine.add(ctx.cached(this).getCanonicalPath());
		cmdLine.add(deprefs.getCanonicalPath());
		cmdLine.addAll(arguments);

		ScriptGenerated.execute(runDir, cmdLine);
	}

}
