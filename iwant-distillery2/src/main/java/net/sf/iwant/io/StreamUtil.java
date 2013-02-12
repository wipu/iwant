package net.sf.iwant.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.iwant.entry2.Iwant2;

public class StreamUtil {

	public static String toString(InputStream in) {
		return Iwant2.toString(in);
	}

	public static void pipe(InputStream in, OutputStream out) {
		Iwant2.pipe(in, out);
	}

	public static void pipeAndClose(InputStream in, OutputStream out) {
		try {
			Iwant2.pipe(in, out);
		} finally {
			try {
				tryToClose(in);
			} finally {
				tryToClose(out);
			}
		}
	}

	public static void tryToClose(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
