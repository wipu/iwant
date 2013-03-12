package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import net.sf.iwant.api.WsInfo;
import net.sf.iwant.entry.Iwant.IwantException;

public class WsInfoFileImpl implements WsInfo {

	private final String wsName;
	private final File wsRoot;
	private final File wsdefdefModule;
	private final File wsdefdefJava;
	private final String wsdefdefClass;
	private final String relativeAsSomeone;

	public WsInfoFileImpl(Reader in, File wsInfo, File asSomeone)
			throws IOException {
		Properties p = new Properties();
		try {
			p.load(in);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read " + wsInfo, e);
		}
		File iHaveConf = wsInfo.getParentFile();
		wsName = property(p, wsInfo, "WSNAME");
		wsRoot = new File(iHaveConf, property(p, wsInfo, "WSROOT"))
				.getCanonicalFile();
		wsdefdefModule = new File(iHaveConf, property(p, wsInfo,
				"WSDEFDEF_MODULE"));
		wsdefdefClass = property(p, wsInfo, "WSDEFDEF_CLASS");
		wsdefdefJava = wsdefdefJava(wsdefdefSrc(), wsdefdefClass);
		relativeAsSomeone = FileUtil.relativePathOfFileUnderParent(asSomeone,
				wsRoot);
	}

	private static File wsdefdefJava(File wsdefdefSrc, String wsdefClass) {
		String java = wsdefClass.replaceAll("\\.", "/") + ".java";
		return new File(wsdefdefSrc, java);
	}

	private static String property(Properties p, File wsInfo, String key) {
		String value = p.getProperty(key);
		if (value == null) {
			throw new IwantException("Please specify " + key + " in " + wsInfo);
		}
		return value;
	}

	@Override
	public String wsName() {
		return wsName;
	}

	@Override
	public File wsRoot() {
		return wsRoot;
	}

	@Override
	public File wsdefdefModule() {
		try {
			return wsdefdefModule.getCanonicalFile();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public File wsdefdefSrc() {
		return new File(wsdefdefModule(), "src/main/java");
	}

	@Override
	public String wsdefClass() {
		return wsdefdefClass;
	}

	@Override
	public File wsdefdefJava() {
		try {
			return wsdefdefJava.getCanonicalFile();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public String wsdefdefPackage() {
		return wsdefdefClass.substring(0, wsdefdefClass.lastIndexOf('.'));
	}

	@Override
	public String wsdefdefClassSimpleName() {
		return wsdefdefClass.substring(wsdefdefClass.lastIndexOf('.') + 1);
	}

	@Override
	public String relativeAsSomeone() {
		return relativeAsSomeone;
	}

}
