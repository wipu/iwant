package net.sf.iwant.io;

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

}
