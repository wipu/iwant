package org.fluentjava.iwant.coreservices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry2.Iwant2;

/**
 * TODO remove need for this class
 */
public class FileUtil {

	public static String relativePathOfFileUnderParent(File child,
			File parent) {
		File absChild = child.getAbsoluteFile();
		File absParent = parent.getAbsoluteFile();
		List<String> names = new LinkedList<>();
		File parentCandidate = absChild;
		while (true) {
			if (parentCandidate == null) {
				// we are already at root, not a child of parent
				throw new IllegalArgumentException(
						absChild + " is not a child of " + absParent);
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
		for (Iterator<String> iterator = names.iterator(); iterator
				.hasNext();) {
			String name = iterator.next();
			b.append(name);
			if (iterator.hasNext()) {
				b.append("/");
			}
		}
		return b.toString();
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		InputStream in = new FileInputStream(sourceFile);
		OutputStream out = new FileOutputStream(destFile);
		StreamUtil.pipeAndClose(in, out);
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
				try {
					StreamUtil.tryToClose(in);
				} finally {
					StreamUtil.tryToClose(out);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String contentAsString(File file) {
		return Iwant2.contentAsString(file);
	}

	public static File textFileEnsuredToHaveContent(File file, String content) {
		return Iwant.textFileEnsuredToHaveContent(file, content);
	}

	public static int copyMissingFiles(File from, File to) throws IOException {
		int count = 0;
		for (File child : from.listFiles()) {
			File toChild = new File(to, child.getName());
			if (!toChild.exists()) {
				count += copyRecursively(child, toChild);
				continue;
			}
			if (child.isDirectory()) {
				count += copyMissingFiles(child, toChild);
			}
		}
		return count;
	}

	public static int copyRecursively(File from, File to) throws IOException {
		return copyRecursively(from, to, false);
	}

	public static int copyRecursively(File from, File to,
			boolean includeSvnDirs) throws IOException {
		if (!includeSvnDirs && ".svn".equals(from.getName())) {
			// TODO handle svn filtering only once
			return 0;
		}
		if (from.isDirectory()) {
			int count = 0;
			Iwant.mkdirs(to);
			for (File child : from.listFiles()) {
				File toChild = new File(to, child.getName());
				count += copyRecursively(child, toChild, includeSvnDirs);
			}
			return count;
		} else {
			FileUtil.copyFile(from, to);
			to.setExecutable(from.canExecute());
			return 1;
		}
	}

}
