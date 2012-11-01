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
	private final File wsdefdefSrc;
	private final File wsdefdefJava;
	private final String wsdefdefClass;

	public WsInfoFileImpl(Reader in, File wsInfo) throws IOException {
		Properties p = new Properties();
		try {
			p.load(in);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read " + wsInfo, e);
		}
		File iHave = wsInfo.getParentFile();
		wsName = property(p, wsInfo, "WSNAME");
		wsRoot = new File(iHave, property(p, wsInfo, "WSROOT"))
				.getCanonicalFile();
		wsdefdefSrc = new File(iHave, property(p, wsInfo, "WSDEF_SRC"));
		wsdefdefClass = property(p, wsInfo, "WSDEF_CLASS");
		wsdefdefJava = wsdefdefJava(wsdefdefSrc, wsdefdefClass);
	}

	private static File wsdefdefJava(File wsdefSrc, String wsdefClass) {
		String java = wsdefClass.replaceAll("\\.", "/") + ".java";
		return new File(wsdefSrc, java);
	}

	private static String property(Properties p, File wsInfo, String key) {
		String value = p.getProperty(key);
		if (value == null) {
			throw new IwantException("Please specify " + key + " in " + wsInfo);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.iwant.entry3.WsInfo#wsName()
	 */
	@Override
	public String wsName() {
		return wsName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.iwant.entry3.WsInfo#wsRoot()
	 */
	@Override
	public File wsRoot() {
		return wsRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.iwant.entry3.WsInfo#wsdefdefSrc()
	 */
	@Override
	public File wsdefdefSrc() {
		try {
			return wsdefdefSrc.getCanonicalFile();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
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

}
