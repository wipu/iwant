package net.sf.iwant.entry3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry2.Iwant2;
import net.sf.iwant.io.StreamUtil;

/**
 * TODO remove need for this class
 */
public class FileUtil {

	public static String relativePathOfFileUnderParent(File child, File parent) {
		File absChild = child.getAbsoluteFile();
		File absParent = parent.getAbsoluteFile();
		List<String> names = new LinkedList<String>();
		File parentCandidate = absChild;
		while (true) {
			if (parentCandidate == null) {
				// we are already at root, not a child of parent
				throw new IllegalArgumentException(absChild
						+ " is not a child of " + absParent);
			}
			if (absParent.equals(parentCandidate)) {
				// tracking ready
				break;
			} else {
				names.add(0, parentCandidate.getName());
				parentCandidate = parentCandidate.getParentFile();
			}
		}
		StringBuilder b = new StringBuilder();
		for (Iterator<String> iterator = names.iterator(); iterator.hasNext();) {
			String name = iterator.next();
			b.append(name);
			if (iterator.hasNext()) {
				b.append("/");
			}
		}
		return b.toString();
	}

	/**
	 * Copied from
	 * http://stackoverflow.com/questions/106770/standard-concise-way
	 * -to-copy-a-file-in-java
	 * 
	 * No dedicated test for this, TODO use commons-io when easy build-wise
	 */
	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static byte[] contentAsBytes(File file) {
		try {
			FileInputStream in = null;
			ByteArrayOutputStream out = null;
			try {
				in = new FileInputStream(file);
				out = new ByteArrayOutputStream();
				StreamUtil.pipe(in, out);
				return out.toByteArray();
			} finally {
				StreamUtil.tryToClose(in);
				StreamUtil.tryToClose(out);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String contentAsString(File file) {
		return Iwant2.contentAsString(file);
	}

	public static File newTextFile(File file, String content) {
		return Iwant.newTextFile(file, content);
	}

}
