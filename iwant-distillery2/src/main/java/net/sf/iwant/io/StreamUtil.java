package net.sf.iwant.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtil {

	public static String toString(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pipe(in, out);
		return out.toString();
	}

	/**
	 * TODO use nio transfer?
	 */
	public static void pipe(InputStream in, OutputStream out) {
		try {
			byte[] buf = new byte[8192];
			while (true) {
				int bytesRead = in.read(buf);
				if (bytesRead < 0) {
					return;
				}
				out.write(buf, 0, bytesRead);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
